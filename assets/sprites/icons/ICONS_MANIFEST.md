# Pixel-art Icons Manifest

Le icone sostituiscono i Material Symbols usati nei mockup Stitch. Stile coerente
col font bitmap del gioco: pixel-art a 16x16 (alcune 24x24 per HUD principale),
palette ristretta neon (vedi `Theme.Palette`), niente anti-aliasing.

## Lista icone necessarie (31 totali)

### Navigation / Menu
- [ ] `play_arrow.png` — start / resume gameplay
- [ ] `replay.png` — restart level
- [ ] `pause.png` — pausa
- [ ] `home.png` — cabinet/home
- [ ] `arrow_back.png` — back nei sub-screen
- [ ] `close.png` — chiudi / NO
- [ ] `power_settings_new.png` — exit / shutdown

### Game UI
- [ ] `videogame_asset.png` — logo header
- [ ] `joystick.png` — controller / cabinet decor
- [ ] `sports_esports.png` — gamepad decor
- [ ] `favorite.png` — vita (filled)
- [ ] `favorite_outline.png` — vita persa (dimmed)
- [ ] `monetization_on.png` — coin / credit
- [ ] `star.png` — challenge / achievement unlocked
- [ ] `lock.png` — challenge locked
- [ ] `trophy.png` — high score / hall of fame

### Settings
- [ ] `settings.png` — menu settings
- [ ] `settings_input_component.png` — input config
- [ ] `videocam.png` — CRT filter / graphics
- [ ] `volume_up.png` — audio master
- [ ] `music_note.png` — music volume
- [ ] `vibration.png` — haptics toggle
- [ ] `language.png` — i18n
- [ ] `accessibility.png` — reduce motion

### Power-ups (in-game falling pills)
- [ ] `pu_expand.png` — E (paddle larger)
- [ ] `pu_slow.png` — S (ball slower)
- [ ] `pu_laser.png` — L (laser paddle)
- [ ] `pu_multi.png` — M (multi-ball)
- [ ] `pu_catch.png` — C (sticky paddle)
- [ ] `pu_life.png` — 1UP
- [ ] `pu_warp.png` — W (skip level)

### Misc
- [ ] `memory.png` — boot screen HW info
- [ ] `signal_cellular_alt.png` — boot screen network

## Convenzioni file

- Formato: PNG indicizzato (8-bit), trasparenza alpha
- Dimensione: 16x16 px (icone UI) o 24x24 (HUD vite/score, power-up)
- Palette: solo colori da `core/.../theme/Theme.kt` + bianco/nero
- Naming: lowercase, snake_case, allineato ai nomi Material Symbols originali
- Output finale: atlante `icons.atlas` + `icons.png` generato con TexturePacker

## Pipeline atlas

```bash
# Dopo aver disegnato i singoli PNG in assets/sprites/icons/raw/
gdx-tools texturepacker assets/sprites/icons/raw assets/sprites icons
```
