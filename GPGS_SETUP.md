# Google Play Games Services — Setup

Guida operativa per agganciare Arkamadoid a GPGS (leaderboard + achievement
online). Finché `gpgs_app_id` in `android/src/main/res/values/strings.xml`
resta `000000000000`, l'integrazione è disabilitata e tutte le chiamate
diventano no-op via `NoopGpgsService` (vedi `AndroidPlatformServices.isGpgsConfigured()`).

---

## Prerequisiti

- Account Google Play Console attivo (€25 una tantum).
- App già creata sul Play Console (anche bozza, non serve pubblicata).
- `applicationId` Android stabile (in `android/build.gradle.kts`: `com.arkamadoid`).

---

## 1. Crea il progetto Play Games Services

1. Apri il **[Play Console](https://play.google.com/console/)** → la tua app.
2. **Crescita & monetizzazione → Play Games Services → Configurazione e gestione → Configurazione**.
3. Scegli "Sì, il mio gioco utilizza già le API di Google" se hai già un progetto Firebase/Google Cloud collegato; altrimenti crea un nuovo progetto.
4. Inserisci un nome interno (es. "Arkamadoid GPGS"), salva.
5. Copia l'**APP ID** (numerico, es. `123456789012`) → finisce in `gpgs_app_id`.

```xml
<!-- android/src/main/res/values/strings.xml -->
<string name="gpgs_app_id" translatable="false">123456789012</string>
```

## 2. Definisci i credentials (firma APK)

1. Sempre in Play Games Services → **Credentials**.
2. Aggiungi credential di tipo "Game server" e "Android".
3. Per Android serve il **SHA-1 della keystore di firma**. Estrailo:
   ```bash
   keytool -list -v -keystore release-keystore.jks -alias arkamadoid
   ```
   Cerca "SHA1:" e incolla nel form Play Console.
4. Se usi la firma gestita da Google Play (App Signing), aggiungi anche il SHA-1 della upload key (Play Console → Setup → App integrity).

## 3. Definisci i Leaderboard (3)

Play Games Services → **Leaderboard → Add leaderboard**. Per ognuno:

| Nome interno | Score format | Order | Display name (IT)  | Display name (EN) |
|---|---|---|---|---|
| `arcade`   | Numeric | Higher is better | Punteggi Arcade   | Arcade Scores  |
| `endless`  | Numeric | Higher is better | Punteggi Endless  | Endless Scores |
| `daily`    | Numeric | Higher is better | Sfida Giornaliera | Daily Challenge |

Dopo aver salvato ognuno, Play Console ti dà un **leaderboard ID** in formato `CgkI...`. Incollalo nelle resource string corrispondenti:

```xml
<!-- android/src/main/res/values/strings.xml -->
<string name="gpgs_leaderboard_arcade"  translatable="false">CgkI__________________</string>
<string name="gpgs_leaderboard_endless" translatable="false">CgkI__________________</string>
<string name="gpgs_leaderboard_daily"   translatable="false">CgkI__________________</string>
```

## 4. Definisci gli Achievement (9)

Play Games Services → **Achievement → Add achievement**. Per ognuno:

| Local ID | Display name (EN) | Display name (IT) | Description (EN) | Description (IT) | Type | Hidden |
|---|---|---|---|---|---|---|
| `first_brick`    | First Blood      | Primo Sangue       | Destroy your first brick                  | Distruggi il tuo primo brick                       | Standard    | No |
| `combo_x2`       | On Fire          | In Fiamme          | Reach a x2 combo multiplier               | Raggiungi il moltiplicatore combo x2               | Standard    | No |
| `combo_x4`       | Unstoppable      | Inarrestabile      | Reach a x4 combo multiplier               | Raggiungi il moltiplicatore combo x4               | Standard    | No |
| `boss_first`     | First Guardian   | Primo Guardiano    | Defeat your first boss                    | Sconfiggi il tuo primo boss                        | Standard    | No |
| `boss_final`     | Omega Down       | Omega Abbattuto    | Clear the final sector boss               | Sconfiggi il boss del settore finale               | Standard    | Yes |
| `pixel_perfect`  | Pixel Perfect    | Pixel Perfect      | Clear a sector without losing a ball      | Completa un settore senza perdere palle            | Standard    | No |
| `no_power`       | Purist           | Purista            | Clear a sector without picking power-ups  | Completa un settore senza raccogliere power-up     | Standard    | No |
| `endless_30`     | Deep Runner      | Esploratore        | Reach sector 30 in ENDLESS                | Raggiungi il settore 30 in ENDLESS                 | Standard    | No |
| `centurion`      | Centurion        | Centurione         | Score 100,000 in a single run             | Totalizza 100.000 punti in una singola run         | Standard    | No |

Per ognuno Play Console ti dà un **achievement ID** in formato `CgkI...`. Incollali:

```xml
<string name="gpgs_achievement_first_brick"   translatable="false">CgkI__________________</string>
<string name="gpgs_achievement_combo_x2"      translatable="false">CgkI__________________</string>
<string name="gpgs_achievement_combo_x4"      translatable="false">CgkI__________________</string>
<string name="gpgs_achievement_boss_first"    translatable="false">CgkI__________________</string>
<string name="gpgs_achievement_boss_final"    translatable="false">CgkI__________________</string>
<string name="gpgs_achievement_pixel_perfect" translatable="false">CgkI__________________</string>
<string name="gpgs_achievement_no_power"      translatable="false">CgkI__________________</string>
<string name="gpgs_achievement_endless_30"    translatable="false">CgkI__________________</string>
<string name="gpgs_achievement_centurion"     translatable="false">CgkI__________________</string>
```

## 5. Tester e pubblicazione

1. In Play Games Services → **Testing**, aggiungi il tuo account Google come tester (necessario PRIMA di poter testare gli unlock — altrimenti vedi errore "not authorized").
2. Genera una build firmata release: `./gradlew :android:assembleRelease`.
3. Installa l'APK sul telefono con l'account Google autorizzato.
4. Avvia il gioco → la sign-in silenziosa parte automaticamente al boot (`ArkamadoidGame.create()`).
5. Distruggi il primo brick → dovresti vedere il popup "FIRST BLOOD" + il toast nativo di Play Games.

Quando tutto funziona, **publish Play Games Services** (bottone in alto a destra). Senza publish, gli achievement sono visibili solo ai tester.

## 6. Verifica integrazione

- Codice locale è in `core/.../achievements/Achievement.kt` (ID locali) e `core/.../screens/GameplayScreen.kt#tryUnlockAchievement` (popup + chiamata GPGS).
- Lato Android: `AndroidGpgsService.unlockAchievement` traduce l'ID locale via `R.string.gpgs_achievement_<id>` → ID GPGS → chiamata `PlayGames.getAchievementsClient().unlock(...)`.
- Se un achievement non scatta sui server: controlla nella resource string corrispondente che l'ID sia incollato esattamente come fornito dal Console (no spazi, no virgolette, no `id:`).

## 7. Troubleshooting comuni

- **"Sign-in cancelled"**: l'utente non ha account Google Play Games configurato sul device. Niente da fare lato app.
- **"This game has not yet been configured"**: hai dimenticato di pubblicare i Play Games Services (step 5).
- **Achievement non si sblocca**: 1) verifica `isSignedIn` in debug, 2) controlla che la resource string esista e contenga l'ID `CgkI...`, 3) verifica che la build sia firmata con lo stesso keystore registrato nei credentials.
