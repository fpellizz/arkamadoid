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
| Pulire | `./gradlew clean` |

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
