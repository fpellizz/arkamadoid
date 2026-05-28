# Privacy Policy — Arkamadoid

*Ultimo aggiornamento: 28 maggio 2026*

Arkamadoid è un videogioco singleplayer offline sviluppato come progetto indie. Questa policy descrive in modo trasparente quali dati il gioco tratta e quali no.

## In sintesi

- **Nessun dato personale viene raccolto, trasmesso o condiviso** dal gioco stesso.
- Tutto ciò che riguarda i tuoi progressi (punteggi, impostazioni, sblocchi) rimane **solo sul tuo dispositivo**.
- Non sono presenti SDK pubblicitari, analytics o profilazione.

## Dati trattati localmente

Il gioco salva sul tuo dispositivo, tramite il meccanismo standard `SharedPreferences` di Android (o file locali su desktop):

- Punteggi e classifica locale (iniziali a 3 caratteri, ad esempio "BOB", inseriti da te).
- Livello sbloccato e skin sbloccate.
- Impostazioni: volume musica/SFX, modalità input, sensibilità, CRT shader on/off, vibrazione on/off, ridurre animazioni on/off, lingua.
- Best score della Daily Challenge del giorno corrente.

Questi dati **non lasciano mai il dispositivo** salvo che tu non attivi esplicitamente l'integrazione con Google Play Games Services (vedi sotto).

## Servizi opzionali di terze parti

### Google Play Games Services (opzionale)

Su Android, se in futuro deciderai di accedere con il tuo account Google per usare leaderboard online e achievement, Google Play Games tratterà i seguenti dati secondo la propria Privacy Policy: identificativo del giocatore, alias scelto su Google Play Games, punteggi inviati alle classifiche. Questa integrazione è **opt-in**: non viene attivata se non scegli esplicitamente di accedere.

Privacy Policy di Google: [https://policies.google.com/privacy](https://policies.google.com/privacy)

### GitHub Releases (solo build sideload)

Le build distribuite tramite GitHub Releases mostrano una notifica di aggiornamento quando è disponibile una nuova versione. Per controllare l'aggiornamento, il gioco esegue una sola richiesta HTTPS all'API pubblica di GitHub (`api.github.com/repos/fpellizz/arkamadoid/releases/latest`). Non viene inviato nessun dato personale, solo gli header HTTP standard. GitHub può loggare l'indirizzo IP secondo la propria Privacy Policy: [https://docs.github.com/en/site-policy/privacy-policies/github-general-privacy-statement](https://docs.github.com/en/site-policy/privacy-policies/github-general-privacy-statement)

## Permessi richiesti

L'APK Android richiede esclusivamente i permessi necessari al funzionamento del gioco:

- `VIBRATE` — per il feedback aptico (disattivabile da Settings).
- `INTERNET` — per il controllo aggiornamenti GitHub e per Play Games Services (se attivato).

Nessun permesso runtime invasivo (contatti, posizione, microfono, fotocamera, storage esterno) è richiesto né mai sarà.

## Bambini

Il gioco è classificato PEGI 3 / ESRB E. Non raccogliendo dati personali, è sicuro anche per bambini. I genitori che volessero disattivare anche solo il controllo aggiornamenti possono installare l'app dal Play Store invece che dal sideload GitHub.

## Modifiche a questa policy

Eventuali aggiornamenti alla policy saranno pubblicati in questo file e nelle release notes della build successiva.

## Contatti

Per domande sulla privacy: apri un issue su [github.com/fpellizz/arkamadoid](https://github.com/fpellizz/arkamadoid/issues).
