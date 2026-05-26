# Levels

Formato JSON ASCII-grid. Una riga = una fila di mattoni dall'alto verso il basso
(visivamente — `originY` è il bordo superiore della griglia in coordinate logiche).

## Tipi mattone

- `NORMAL` — 1 colpo, 50 pt
- `TOUGH` — 2 colpi, 100 pt
- `VERY_TOUGH` — 3 colpi, 200 pt
- `INDESTRUCTIBLE` — ostacolo permanente
- `EXPLOSIVE` — distrugge gli adiacenti
- `GOLD` — 500 pt
- `EMPTY` — nessun mattone

`legend` mappa caratteri della grid ai tipi sopra. Le lettere N/R/Y nell'esempio
sono tutte `NORMAL` ma servono al renderer per assegnare colori diversi (palette
ispirata ad Arkanoid 1986: bianco, rosso, giallo, verde, blu, magenta...).
