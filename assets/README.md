# Assets

Cartella condivisa fra `:android` (linkata via `sourceSets.main.assets.srcDirs`) e `:desktop` (workingDir di `run`).

## Struttura attesa

```
assets/
├── fonts/        # font bitmap (.fnt + .png) — stile arcade 8x8 / 16x16
├── sprites/      # atlas pixel art (.atlas + .png) — paddle, ball, bricks, powerups, UI
├── audio/
│   ├── music/    # chiptune .ogg loop (menu, gameplay, boss, victory)
│   └── sfx/      # one-shot .ogg (bounce, brick, powerup, coin, gameover...)
├── shaders/      # crt.vert / crt.frag + altri post-fx
├── i18n/         # strings_it.properties, strings_en.properties (libgdx I18NBundle)
└── levels/       # 01.json, 02.json, ... layout dei livelli
```

## In attesa dei mockup

L'UI completa (menu, HUD, attract mode, high score table, bezel arcade)
verrà implementata quando arrivano i mockup grafici. I file degli stub
sono pronti per ricevere asset e logica di rendering.

Drop dei mockup consigliato in `assets/_mockups/` (non viene compilato).
