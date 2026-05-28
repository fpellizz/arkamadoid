# Arkamadoid

Clone Android di Arkanoid con estetica retro-arcade. Engine **LibGDX 1.12 + Kotlin 1.9**, multi-modulo Gradle.

> Vedi [REQUIREMENTS.md](./REQUIREMENTS.md) per requisiti funzionali e direzione artistica.

---

## Prerequisiti

- **JDK 17** (Temurin / Zulu / Oracle)
- **Android SDK** con platform-tools 34 e build-tools 34.0.0
- Variabile `ANDROID_HOME` impostata (o `sdk.dir=...` in `local.properties`)
- Gradle wrapper: incluso (vedi nota sotto)

> **Nota sul wrapper**: il binario `gradle-wrapper.jar` e gli script `gradlew/gradlew.bat` non sono ancora committati per evitare un binario in repo vuoto. Generali una volta con:
> ```bash
> gradle wrapper --gradle-version 8.5
> ```
> richiede un Gradle 8.5+ installato di sistema (`sdk install gradle 8.5` con SDKMAN!).

## Struttura

```
arkamadoid/
├── core/         # logica di gioco platform-independent (Kotlin + LibGDX)
├── android/      # launcher Android + integrazione GPGS
├── desktop/      # launcher LWJGL3 per sviluppo rapido senza emulatore
├── assets/       # sprites, fonts, audio, shader, livelli (condivisi)
├── REQUIREMENTS.md
└── README.md
```

## Comandi utili

| Cosa | Comando |
|---|---|
| Avviare la versione desktop (dev) | `./gradlew :desktop:run` |
| Installare debug APK su device collegato | `./gradlew :android:installDebug` |
| Build release APK | `./gradlew :android:assembleRelease` |
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
binari allegati se il trigger è un tag.

## Roadmap implementazione

Lo scaffolding attuale comprende:

- [x] Build multi-modulo Gradle (root + core + android + desktop)
- [x] Entry point `ArkamadoidGame` + screen stack vuoto
- [x] Entità di gioco (Paddle, Ball, Brick, PowerUp) — dati + bounds
- [x] Configurazione game (`GameConfig`)
- [x] Stub collision resolver, level loader, score manager
- [x] AudioManager / ParticleSystem stub
- [x] Persistenza preferences + SaveData
- [x] Interfaccia PlatformServices + GpgsService (stub Android con Play Games v2)
- [x] Localizzazione IT/EN via I18NBundle
- [x] Shader CRT base (scanlines + chromatic aberration)
- [x] Livello d'esempio `01.json`
- [ ] **In attesa mockup**: rendering UI menu, HUD, attract mode, high score table
- [ ] Implementazione collisioni / fisica arcade
- [ ] Caricamento atlas sprite (quando arrivano)
- [ ] Bilanciamento difficoltà / power-up
- [ ] Integrazione GPGS effettiva (serve app id da Play Console)

## Quando arrivano i mockup

Dropparli in `assets/_mockups/` (non viene compilato). La pipeline di estrazione
asset (atlas TexturePacker, font Hiero o bmfont) verrà aggiunta in seguito.

## Licenza

TBD
