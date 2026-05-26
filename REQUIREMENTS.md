# Arkamadoid — Requisiti e Specifiche Funzionali

> Clone di Arkanoid per Android con estetica retro-arcade ispirata ai cabinati da salagiochi anni '80.

---

## 1. Visione del prodotto

Arkamadoid è un breakout game per dispositivi Android che ricrea l'esperienza emotiva e visiva dei cabinati arcade degli anni '80 (Arkanoid 1986, Breakout, Super Breakout). L'obiettivo è offrire un gameplay immediato, controlli precisi adattati al touch, e una direzione artistica retro-pixel ad alto impatto visivo, in grado di evocare nostalgia senza apparire "vecchio" o trascurato.

**Pillar di design:**
1. **Pick-up-and-play**: una partita inizia entro 2 tap dall'apertura dell'app.
2. **Identità arcade autentica**: ogni elemento UI/UX ricorda un coin-op (attract mode, high score table, CRT scanlines, font bitmap).
3. **Polish moderno**: feedback aptico, particellari, easing animazioni e juiciness contemporanea sotto la pelle retro.

---

## 2. Target e piattaforma

| Voce | Specifica |
|---|---|
| Piattaforma | Android 8.0 (API 26) e superiori |
| Orientamento | Portrait (primario), Landscape (opzionale per tablet) |
| Risoluzione | Adattiva da 720x1280 a 1440x3200, virtual canvas 360x640 dp |
| Input | Touch (drag, tap), opzionale tilt/gamepad |
| Distribuzione | Google Play Store, build APK side-load |
| Target età | 6+ (PEGI 3 / ESRB E) |
| Lingue | Italiano, Inglese (estendibile) |

---

## 3. Requisiti funzionali

### 3.1 Gameplay core (RF-G)

- **RF-G-01** Il giocatore controlla una paddle orizzontale nella parte bassa dello schermo.
- **RF-G-02** Una pallina rimbalza tra paddle, muri laterali, muro superiore e mattoni; l'angolo di rimbalzo sulla paddle dipende dal punto di impatto (5–7 zone, da -75° a +75°).
- **RF-G-03** Distruggere tutti i mattoni distruttibili del livello fa avanzare al livello successivo.
- **RF-G-04** Perdere la pallina (esce dal bordo inferiore) costa una vita. A 0 vite: Game Over.
- **RF-G-05** Vite iniziali: 3. Vita extra ogni 20.000 punti (configurabile).
- **RF-G-06** La velocità della pallina aumenta gradualmente all'interno del livello (soft) e tra livelli (hard).
- **RF-G-07** Mattoni:
  - **Normale** — 1 colpo, 50 pt
  - **Resistente** — 2/3 colpi, 100/200 pt, cambia colore ad ogni hit
  - **Indistruttibile** — non si rompe, serve da ostacolo
  - **Esplosivo** — distrugge i mattoni adiacenti
  - **Mattone d'oro** — 500 pt, raro
- **RF-G-08** Power-up (drop casuale dai mattoni, cadono verso il basso, presi con la paddle):
  - **Expand (E)** — paddle più larga
  - **Slow (S)** — pallina rallentata
  - **Laser (L)** — paddle spara, 2 colpi per mattone normale
  - **Multi (M)** — sdoppia la pallina (fino a 3 contemporanee)
  - **Catch (C)** — la pallina resta attaccata alla paddle, lancio manuale
  - **Life (1UP)** — vita extra
  - **Warp (W)** — apre un varco per saltare al livello successivo
- **RF-G-09** Boss fight ogni 8 livelli (configurabile).
- **RF-G-10** Almeno 24 livelli a lancio + livello bonus, layout a griglia, design hand-crafted.

### 3.2 Controlli (RF-C)

- **RF-C-01** Modalità primaria: **drag** orizzontale ovunque sulla metà bassa dello schermo (la paddle segue il dito con offset, non posizione assoluta sotto il dito).
- **RF-C-02** Modalità alternativa: **touch assoluto** (paddle si allinea al dito); selezionabile da Settings.
- **RF-C-03** Tap singolo per lanciare la pallina dalla paddle (start palla o stato Catch).
- **RF-C-04** Sensibilità regolabile (slider 0.5x — 2.0x).
- **RF-C-05** Pulsante pausa in alto a destra, sempre visibile in-game.
- **RF-C-06** Supporto opzionale gamepad Bluetooth (D-pad + tasto azione).

### 3.3 Modalità di gioco (RF-M)

- **RF-M-01** **Arcade Mode** — campagna a livelli con vite limitate e continue (basati su monete virtuali ricaricabili a tempo).
- **RF-M-02** **Endless Mode** — livelli procedurali infiniti, leaderboard dedicata.
- **RF-M-03** **Daily Challenge** — un livello al giorno, stesso seed per tutti i giocatori, classifica giornaliera.
- **RF-M-04** **Practice** — selezione libera di livelli già sbloccati, senza salvataggio punteggio. Modificatore "zero gravity": pallina con decelerazione nella metà alta dello schermo, power-up senza gravità di caduta. Finalità: allenamento mira.

### 3.4 Progressione e meta (RF-P)

- **RF-P-01** Punteggio per partita e high score persistente.
- **RF-P-02** Top 10 high score locale con iniziali a 3 caratteri (stile arcade).
- **RF-P-03** Leaderboard online opzionale (Google Play Games Services).
- **RF-P-04** Achievements: "Pixel Perfect" (livello senza perdere palla), "Combo X" (N mattoni di fila), "No Power" (livello senza power-up), ecc.
- **RF-P-05** Sblocco progressivo di skin paddle / pallina / palette mattoni.

### 3.5 UI/Menu (RF-U)

- **RF-U-01** **Attract Mode**: dopo 15s di inattività al menu, parte una demo automatica di gameplay con scritta "INSERT COIN — PRESS START" lampeggiante.
- **RF-U-02** Schermata principale con: logo animato, "PRESS START", crediti virtuali, high score corrente.
- **RF-U-03** Schermata high score con tabella top 10 e animazione di inserimento iniziali (joystick virtuale).
- **RF-U-04** Schermata settings: audio (master/music/SFX), input mode, sensibilità, scanlines on/off, vibrazione, lingua.
- **RF-U-05** Pause overlay con: Resume, Restart, Settings, Quit.
- **RF-U-06** Game Over con countdown "CONTINUE? 9... 8..." e opzione di proseguire.
- ~~**RF-U-07** Tutorial al primo avvio~~ — rimosso 2026-05-26: Arkanoid è auto-evidente, basta il microcopy "SLIDE TO STEER" già presente in-game (HUD gameplay).

### 3.6 Audio (RF-A)

- **RF-A-01** Musica chiptune 8/16-bit, almeno 4 tracce (menu, gameplay loop, boss, victory).
- **RF-A-02** SFX retro: rimbalzo (pitch variabile in base alla zona paddle), rottura mattone, power-up, perdita palla, game over, coin insert.
- **RF-A-03** Volume audio separato per musica e SFX.
- **RF-A-04** Audio focus rispettato (pausa automatica con chiamate / altre app).

### 3.7 Persistenza e dati (RF-D)

- **RF-D-01** Salvataggio locale: progressi, high score, settings, skin sbloccate (SharedPreferences / Room).
- **RF-D-02** Backup su Google Drive opzionale (Auto Backup Android).
- **RF-D-03** Nessuna raccolta di PII oltre a quanto richiesto da Play Games Services.

---

## 4. Requisiti non funzionali

### 4.1 Performance (RNF-P)

- **RNF-P-01** Frame rate target: **60 fps** stabile su device di fascia media (es. Pixel 4a, Galaxy A52).
- **RNF-P-02** Tempo di avvio cold start < 2.5s su hardware di riferimento.
- **RNF-P-03** Caricamento livello < 500ms.
- **RNF-P-04** Latency input → reazione paddle < 33ms (2 frame).
- **RNF-P-05** Dimensione APK < 50 MB (asset compressi, no streaming runtime).

### 4.2 Usabilità (RNF-U)

- **RNF-U-01** Una nuova partita raggiungibile in ≤ 2 tap dalla home screen del gioco.
- **RNF-U-02** Tutti i controlli interattivi ≥ 48dp di area touch.
- **RNF-U-03** Contrasto testo/sfondo conforme WCAG AA (4.5:1) nei menu.
- **RNF-U-04** Opzione "Reduce Motion" per ridurre shake/flash (accessibilità + sicurezza fotosensibilità).
- **RNF-U-05** Nessun testo critico bruciato in texture: tutto localizzabile.

### 4.3 Affidabilità (RNF-R)

- **RNF-R-01** Crash-free sessions ≥ 99.5%.
- **RNF-R-02** Stato di gioco salvato a ogni perdita di focus (onPause) per recupero senza perdita progresso.
- **RNF-R-03** Gestione resume da background entro 1s.

### 4.4 Sicurezza/Privacy (RNF-S)

- **RNF-S-01** Privacy policy pubblica linkata dal menu.
- **RNF-S-02** Nessuna richiesta di permessi runtime non necessari (no contatti, no posizione).
- **RNF-S-03** Telemetria anonima opt-in.

---

## 5. Direzione artistica — retro arcade reinterpretato

### 5.1 Stile visivo
- **Pixel art** a risoluzione nativa bassa (es. 240x320 logici) scalata a intero (nearest-neighbor) sullo schermo del device.
- **Palette ristretta** ispirata a hardware d'epoca (NES/Commodore/CPS-1): 16-32 colori per scena, ad alta saturazione e contrasto.
- **Post-processing shader** opzionale:
  - **CRT scanlines** orizzontali con leggera curvatura
  - **Bloom** sui neon
  - **Chromatic aberration** sottile ai bordi
  - **Phosphor glow** sui mattoni distrutti
- **Tipografia**: font bitmap 8x8 e 16x16 stile DOS/CRT (es. ricreazioni di Atari, Namco, Taito). Mai font di sistema.

### 5.2 Linguaggio UI cabinato
- Bordi UI in stile **bezel arcade**: cornici metalliche pittate, sticker, decals.
- Marquee superiore con logo "ARKAMADOID" effetto neon.
- Pulsanti come **tasti arcade colorati** (rosso, giallo, blu) con ombra pressione.
- Joystick virtuale stile **ball-top** Sanwa per l'inserimento iniziali high score.
- Indicatori in-game (vite, punteggio, livello) stile **7-segment display** o LED dot-matrix.
- "INSERT COIN" e "PRESS START" lampeggianti a frequenza arcade autentica (~1.5 Hz).

### 5.3 Feedback e juiciness
- **Screen shake** breve su impatto pallina-mattone resistente e boss hit.
- **Hit-stop** di 1-2 frame su distruzione mattone d'oro o esplosioni.
- **Particelle pixel** alla rottura: 6-12 quadrati colorati con fisica semplice.
- **Trail** della pallina con afterimage a 3 step.
- **Vibrazione aptica** (50-30ms) su impatti significativi, disattivabile.

---

## 6. Architettura tecnica (proposta)

| Layer | Scelta proposta | Razionale |
|---|---|---|
| Engine | **LibGDX** (Java/Kotlin) o **Godot 4 (GDScript)** | Pixel-perfect rendering, leggero, ottima curva su Android |
| Linguaggio | Kotlin (se LibGDX) / GDScript (se Godot) | Idiomi moderni, build ridotte |
| Fisica | Custom 2D (AABB + circle vs AABB) | Controllo totale sugli angoli di rimbalzo arcade-style |
| Audio | OpenAL via engine, formato OGG | Bassa latenza |
| Persistenza | SharedPreferences + JSON locale | Sufficiente, no necessità SQL |
| Online | Google Play Games Services (leaderboard, achievement) | Standard Android |
| Build | Gradle + signing config produzione | — |

> Nota: la fisica della pallina **non deve essere realistica** ma "arcade-correct": angolo deterministico in base alla zona di impatto sulla paddle, nessun effetto di rotazione/spin reale.

---

## 7. Criteri di accettazione (sintesi)

Il prodotto è considerato pronto al rilascio (MVP) quando:

- [ ] 24 livelli giocabili senza crash su 3 device fisici di riferimento.
- [ ] Tutti i power-up (RF-G-08) funzionano e sono bilanciati.
- [ ] Endless Mode + Daily Challenge operativi.
- [ ] Attract mode + high score table in stile arcade implementati.
- [ ] 60 fps stabili su hardware di riferimento.
- [ ] Accessibilità base (reduce motion, scanlines off) presente.
- [ ] Localizzazione IT/EN completa.
- [ ] APK < 50 MB, firmato release.

---

## 8. Out of scope (per MVP)

- Multiplayer online sincrono.
- Editor di livelli per utente.
- Acquisti in-app / monetizzazione (rimandati a post-launch).
- Versione iOS.
- Modalità VR/AR.

---

## 9. Rischi e mitigazioni

| Rischio | Impatto | Mitigazione |
|---|---|---|
| Touch input meno preciso del trackball originale Arkanoid | Alto (gameplay) | Drag-offset come default, sensibilità configurabile, playtest precoce |
| Shader CRT pesante su device low-end | Medio (perf) | Toggle on/off, fallback senza post-processing |
| "Retro" percepito come "trasandato" se mal eseguito | Alto (percezione) | Direzione artistica curata da brief grafico dedicato, mood board, review iterative |
| Bilanciamento difficoltà power-up vs mattoni | Medio (gameplay) | Playtest interno + telemetria opt-in per tuning post-launch |

---

*Documento v0.1 — base per discussione e iterazione.*

---

## Changelog

### 2026-05-26 — Decisioni di design dopo review mockup Stitch
- **Engine**: LibGDX 1.12 + Kotlin 1.9 (era proposta aperta).
- **Direzione artistica**: adottato il design system `assets/_mockups/neon_arcade/DESIGN.md` come north-star (palette M3 dark + neon magenta `#ff00e5` / cyan `#00eefc` / yellow `#dbc900`, font Space Mono, CRT overlay).
- **Bezel cabinet**: visibile sempre, sottile su mobile (2-3 px virtual canvas), spesso su desktop (5 px). Sostituisce la decisione originale "bezel solo desktop" del DESIGN.md per non perdere identità sul target primario Android portrait.
- **Icone**: pixel-art custom 16x16 (24x24 per HUD principale), NON Material Symbols come nei mockup. Manifest in `assets/sprites/icons/ICONS_MANIFEST.md`.
- **Screen aggiunti**: `ModeSelectScreen` (RF-M). Mockup in arrivo da Stitch.
- **Screen ancora mancante**: GameplayScreen completo (rigenerazione Stitch in corso). Riferimento parziale: pause_screen mostra paddle metallica cyan + ball bianca + mattoni magenta/rosa su griglia 8-col.
- **Token centralizzati**: nuovo file `core/.../theme/Theme.kt` come singola sorgente per palette/spacing/fontsize/animazioni.

### Adattamenti Stitch → Android nativo da implementare
- Settings > Input: i mockup mostrano binding tastiera PC (A/D/SPACE/ESC). Su Android sostituire con: input mode toggle (drag-offset vs touch-absolute), sensitivity slider, gamepad on/off, haptics toggle.
- Naming nav: uniformare tra "CABINET/ARCADE/RANKING/SYSTEM" (title) e voci menu (START/MODES/HIGH SCORES/SETTINGS/EXIT).
- Vite confermate come cuori (`favorite` icon), non LED dot-matrix come prima ipotesi.

### 2026-05-26 (sera) — Decisioni post-review gameplay mockup
- **Mattoni**: bordi `rounded-sm` (~2px) come mockup, NON sharp 0px come dichiarato in DESIGN.md. Il DESIGN.md va considerato indicativo, il mockup è la fonte di verità ultima.
- **HUD microcopy**: hardcoded in inglese (`DATA_STORE` / `SECTOR` / `INTEGRITY`) anche con lingua IT. Non si traduce: è "terminologia tecnica di sistema" coerente col worldbuilding cyberpunk. Stringhe `hud.score/level/lives` rimosse dai bundle i18n.
- **Touch input default**: `DRAG_OFFSET` (paddle segue il dito con offset, non si allinea sotto). `TOUCH_ABSOLUTE` resta selezionabile da Settings come modalità alternativa. Lo script JS del mockup usa absolute ma è solo una demo HTML, non impone la modalità.
- **Bottom nav in gameplay**: nascosto durante la partita (palla in volo / vite > 0). Riappare in pausa, game over, transizioni. Massimizza immersione.

### Elementi gameplay ancora da definire (non presenti nel mockup)
- Visualizzazione mattoni TOUGH (multi-HP, cambio colore), VERY_TOUGH, GOLD, EXPLOSIVE, INDESTRUCTIBLE.
- Visualizzazione power-up in caduta (pill colorate E/S/L/M/C/1UP/W).
- Trail della pallina (afterimage 3-step).
- Combo counter / multiplier on-screen.
- Boss visual.

### 2026-05-26 (tardi) — Post-review Mode Select + naming nav
- **Mode Select mockup approvato**. Card per modalità con colore neon dedicato (ARCADE magenta, ENDLESS cyan, DAILY yellow, PRACTICE neutro). Coerente con HUD gameplay.
- **PRACTICE arricchita**: aggiunto modificatore "zero gravity" come da mockup (pallina decelera nella metà alta, power-up senza gravità). Differenzia da ARCADE senza punteggio.
- **TutorialScreen**: confermato eliminato. Arkanoid è auto-evidente, il microcopy in-game "SLIDE TO STEER" basta.
- **Naming bottom nav UNIFICATO**: `HOME / PLAY / SCORES / SETTINGS`. Label intere (mai troncate), maiuscolo, max 8 caratteri. Sostituisce tutte le varianti dei mockup (Cabinet/Arcade/Ranking/System, Home/Play/Scores/Profile, Home/Play/Rank/Set).
  - HOME (icona `home`) → torna a MainMenu
  - PLAY (icona `videogame_asset` filled) → riprende partita o porta a ModeSelect
  - SCORES (icona `leaderboard`) → HighScoreScreen (locali + GPGS)
  - SETTINGS (icona `settings`) → SettingsScreen
- **Nav nascosto** durante gameplay attivo (ball in volo).

