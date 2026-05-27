#!/usr/bin/env bash
# Arkamadoid - build + (optional) install + (optional) launch on Android device.
#
# Flusso:
#   1. Build sempre (./gradlew :android:assembleDebug)
#   2. Chiede se installare sul device
#   3. Se sì, chiede se rimuovere la versione già installata prima di installare
#   4. Chiede se lanciare l'app subito dopo
#
# Flag opzionali:
#   -y / --yes        accetta tutti i prompt (CI / batch)
#   -l / --logcat     dopo il launch attacca logcat filtrato sul PID
#   -h / --help       mostra questo aiuto

set -euo pipefail

# -------- paths & costanti --------
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
APP_PKG="com.arkamadoid.debug"
LAUNCHER="${APP_PKG}/com.arkamadoid.android.AndroidLauncher"
APK_PATH="${PROJECT_ROOT}/android/build/outputs/apk/debug/android-debug.apk"

# -------- colori (solo se tty) --------
if [[ -t 1 ]]; then
    C_RED=$'\033[31m'; C_GRN=$'\033[32m'; C_YLW=$'\033[33m'
    C_CYN=$'\033[36m'; C_DIM=$'\033[2m';  C_RST=$'\033[0m'
else
    C_RED=""; C_GRN=""; C_YLW=""; C_CYN=""; C_DIM=""; C_RST=""
fi
log()  { printf '%s▸%s %s\n' "${C_CYN}" "${C_RST}" "$*"; }
ok()   { printf '%s✓%s %s\n' "${C_GRN}" "${C_RST}" "$*"; }
warn() { printf '%s!%s %s\n' "${C_YLW}" "${C_RST}" "$*"; }
die()  { printf '%s✗%s %s\n' "${C_RED}" "${C_RST}" "$*" >&2; exit 1; }

# -------- parse flag --------
ASSUME_YES=0
DO_LOGCAT=0
for arg in "$@"; do
    case "$arg" in
        -y|--yes)    ASSUME_YES=1 ;;
        -l|--logcat) DO_LOGCAT=1 ;;
        -h|--help)
            sed -n '2,15p' "$0" | sed 's/^# \{0,1\}//'
            exit 0
            ;;
        *) die "flag sconosciuto: $arg (prova --help)" ;;
    esac
done

ask() {
    # ask "domanda?" default(Y|n)  → ritorna 0=si 1=no
    local q="$1" def="${2:-Y}" hint reply
    if [[ "$ASSUME_YES" == "1" ]]; then return 0; fi
    [[ "$def" == "Y" ]] && hint="[Y/n]" || hint="[y/N]"
    read -r -p "$(printf '%s?%s %s %s ' "${C_YLW}" "${C_RST}" "$q" "$hint")" reply || reply=""
    reply="${reply:-$def}"
    [[ "$reply" =~ ^[YySs]$ ]]
}

# -------- JAVA_HOME (SDKMAN) --------
if [[ -z "${JAVA_HOME:-}" ]]; then
    SDKMAN_JAVA="${HOME}/.sdkman/candidates/java/current"
    if [[ -d "$SDKMAN_JAVA" ]]; then
        export JAVA_HOME="$SDKMAN_JAVA"
        log "JAVA_HOME → ${C_DIM}${JAVA_HOME}${C_RST}"
    else
        warn "JAVA_HOME non settato e SDKMAN java/current non trovato — uso quello di sistema"
    fi
fi

cd "$PROJECT_ROOT"

# ============ 1. BUILD ============
log "Build APK debug…"
./gradlew :android:assembleDebug
[[ -f "$APK_PATH" ]] || die "APK non trovato in $APK_PATH"
ok "APK pronto: ${C_DIM}${APK_PATH#${PROJECT_ROOT}/}${C_RST} ($(du -h "$APK_PATH" | cut -f1))"

# ============ 2. INSTALL? ============
echo
if ! ask "Installare l'APK sul device?" Y; then
    log "Skip install. Fine."
    exit 0
fi

# adb disponibile?
command -v adb >/dev/null || die "adb non trovato nel PATH"

# device collegato?
DEVICES=$(adb devices | awk 'NR>1 && $2=="device" {print $1}')
DEV_COUNT=$(printf '%s\n' "$DEVICES" | grep -c . || true)
if [[ "$DEV_COUNT" -eq 0 ]]; then
    die "Nessun device connesso (adb devices vuoto). Abilita USB debug e ricollega."
elif [[ "$DEV_COUNT" -gt 1 ]]; then
    warn "Più device connessi:"
    printf '%s\n' "$DEVICES" | sed 's/^/   /'
    die "Setta ANDROID_SERIAL o scollega quelli che non servono."
fi
DEVICE_ID="$DEVICES"
ok "Device: ${C_DIM}${DEVICE_ID}${C_RST}"

# ============ 3. UNINSTALL? ============
if adb -s "$DEVICE_ID" shell pm list packages | grep -q "^package:${APP_PKG}$"; then
    warn "${APP_PKG} è già installato sul device."
    if ask "Rimuoverlo prima di installare la nuova versione?" N; then
        log "Uninstall ${APP_PKG}…"
        adb -s "$DEVICE_ID" uninstall "$APP_PKG" >/dev/null && ok "Rimosso." || die "Uninstall fallito."
    else
        log "Procedo con install -r (aggiorno l'esistente)."
    fi
fi

# ============ 4. INSTALL ============
log "Install APK…"
adb -s "$DEVICE_ID" install -r "$APK_PATH" >/dev/null
ok "Installato."

# ============ 5. LAUNCH? ============
echo
if ! ask "Lanciare l'app adesso?" Y; then
    log "Skip launch. Fine."
    exit 0
fi
log "Launch ${LAUNCHER}…"
adb -s "$DEVICE_ID" shell am start -n "$LAUNCHER" >/dev/null
ok "Avviato."

# ============ 6. LOGCAT (opzionale) ============
if [[ "$DO_LOGCAT" == "1" ]]; then
    sleep 1
    PID="$(adb -s "$DEVICE_ID" shell pidof "$APP_PKG" | tr -d '\r\n' || true)"
    if [[ -z "$PID" ]]; then
        warn "PID non trovato — logcat completo (Ctrl-C per uscire)."
        exec adb -s "$DEVICE_ID" logcat
    fi
    log "logcat --pid=${PID} (Ctrl-C per uscire)"
    exec adb -s "$DEVICE_ID" logcat --pid="$PID"
fi
