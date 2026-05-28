# Arkamadoid

Clone Android di Arkanoid con estetica retro-arcade cyberpunk. Engine **LibGDX 1.14 + Kotlin 1.9**, multi-modulo Gradle.

> Vedi [REQUIREMENTS.md](./REQUIREMENTS.md) per requisiti funzionali e direzione artistica.

---

## Features attuali

- **24 livelli hand-crafted** + generatore procedurale infinito per ENDLESS
- **4 modalità di gioco**: ARCADE (storia), ENDLESS (sopravvivenza con boss ogni 8 livelli), DAILY (un livello al giorno con seed condiviso), PRACTICE (zero death, slow-mo opzionale)
- **9 power-up**: EXPAND, SLOW, LASER, MULTI, CATCH, LIFE, WARP, BLACKBALL (void mode), ZERO-GRAVITY
- **Boss fight** ogni 8 livelli (sector 8/16/24) + boss procedurali in ENDLESS, oscillazione orizzontale, HP bar, reward power-up shower + 5000 punti
- **Combo counter** con moltiplicatore score fino a ×4
- **9 achievement** con popup HUD e schermata dedicata (counter X/9, hidden achievement support)
- **GPGS wiring** completo (sign-in silent, leaderboard, achievement); attivabile configurando il Play Console (vedi [GPGS_SETUP.md](./GPGS_SETUP.md))
- **Localizzazione IT/EN** completa (rilevamento system locale + toggle in Settings)
- **CRT shader** opzionale (scanlines + chromatic aberration), reduce motion, haptic feedback, audio focus loss
- **Update notifier** custom per APK side-loaded da GitHub Releases
- **Privacy policy** in-repo ([PRIVACY.md](./PRIVACY.md))

## Prerequisiti

- **JDK 17** (Temurin / Zulu / Oracle)
- **Android SDK** con platform-tools 34 e build-tools 34.0.0
- Variabile `ANDROID_HOME` impostata (o `sdk.dir=...` in `local.properties`)
- Gradle wrapper: incluso nel repo (`./gradlew` funziona out-of-the-box)

## Struttura

```
arkamadoid/
├── core/                  # logica di gioco platform-independent (Kotlin + LibGDX)
├── android/               # launcher Android + integrazione GPGS
├── desktop/               # launcher LWJGL3 per sviluppo rapido senza emulatore
├── assets/                # sprites, fonts, audio, shader, livelli, i18n
│   ├── levels/            # 24 livelli hand-crafted JSON
│   └── i18n/              # bundle IT/EN
├── .github/workflows/     # CI/CD (build per branch, release firmata su tag)
├── GPGS_SETUP.md          # guida setup Google Play Games Services
├── PRIVACY.md             # privacy policy pubblica
├── REQUIREMENTS.md        # specifiche funzionali e direzione artistica
└── README.md
```

## Comandi utili

| Cosa | Comando |
|---|---|
| Avviare la versione desktop (dev) | `./gradlew :desktop:run` |
| Installare debug APK su device collegato | `./gradlew :android:installDebug` |
| Build release APK firmato | `./gradlew :android:assembleRelease` |
| Build release App Bundle (per Play Store) | `./gradlew :android:bundleRelease` |
| Pulire | `./gradlew clean` |

## Build release (firma)

Per produrre APK/AAB firmati serve un keystore e le sue credenziali.

### Locale

1. Genera (o procura) `release-keystore.jks` (resta `gitignored`):
   ```bash
   keytool -genkeypair -v \
     -keystore release-keystore.jks -storetype PKCS12 \
     -keyalg RSA -keysize 2048 -validity 10000 \
     -alias arkamadoid \
     -dname "CN=Arkamadoid, OU=Indie, O=Arkamadoid Industries, C=IT"
   ```
2. Crea `keystore.properties` in radice (anch'esso `gitignored`):
   ```
   storeFile=release-keystore.jks
   storePassword=<la-tua-password>
   keyAlias=arkamadoid
   keyPassword=<la-tua-password>
   ```
3. `./gradlew :android:assembleRelease` — APK firmato finisce in
   `android/build/outputs/apk/release/`.

**Conserva il keystore in un password manager o backup offline**:
se lo perdi non potrai più pubblicare aggiornamenti dell'app firmati
con la stessa chiave su Play Store.

### CI (GitHub Actions)

Il workflow `release.yml` parte su tag `v*.*.*` o `workflow_dispatch`.
Richiede 3 Secrets nel repo:

| Secret | Contenuto |
|---|---|
| `RELEASE_KEYSTORE_BASE64` | output di `base64 -w0 release-keystore.jks` |
| `RELEASE_KEYSTORE_PASSWORD` | password del keystore |
| `RELEASE_KEY_PASSWORD` | password della chiave (può coincidere con quella del keystore) |

Output: APK + AAB come artefatti del run, e una GitHub Release con i
binari allegati se il trigger è un tag. Gli utenti già installati
vedono un banner "AGGIORNAMENTO DISPONIBILE" al prossimo avvio del
gioco (UpdateChecker polla `api.github.com/repos/.../releases/latest`).

### Rilasciare una nuova versione

```bash
git tag -a v0.X.0 -m "release notes"
git push origin v0.X.0
```

La pipeline `release.yml` builda APK + AAB firmati e pubblica una
GitHub Release con i binari allegati e il changelog automatico.

## Documentazione operativa

- [GPGS_SETUP.md](./GPGS_SETUP.md) — guida step-by-step per agganciare Play Games Services (leaderboard + achievement online).
- [PRIVACY.md](./PRIVACY.md) — privacy policy linkata dal menu Settings dell'app.
- [REQUIREMENTS.md](./REQUIREMENTS.md) — specifiche funzionali, direzione artistica, criteri MVP.

## Stato MVP

Vedi [REQUIREMENTS.md §7](./REQUIREMENTS.md#7-criteri-di-accettazione-sintesi) per i criteri di accettazione completi.

- [x] 24 livelli giocabili senza crash
- [x] Tutti i power-up RF-G-08 funzionanti (+ BLACKBALL e ZERO-GRAVITY extra)
- [x] Endless Mode + Daily Challenge operativi
- [x] Attract mode + high score table arcade
- [x] Accessibilità base (reduce motion, scanlines off)
- [x] Localizzazione IT/EN completa
- [x] APK < 50 MB, firmato release
- [x] Privacy policy linkata
- [ ] 60fps stabili verificati su 3 device fisici di riferimento
- [ ] Configurazione GPGS sul Play Console (codice pronto, manca solo l'azione su Play Console — vedi GPGS_SETUP.md)
- [ ] Pubblicazione su Play Store

## Licenza

TBD
