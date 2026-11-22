# Regole di sviluppo del codice – FTC

Questo documento raccoglie le regole che tutti i membri del team devono seguire quando lavorano sul codice del robot. Lavorando in tanti sullo stesso progetto, un metodo condiviso è indispensabile per evitare conflitti, perdite di informazioni e codice incomprensibile.

## Indice

1. [Changelog](#1-changelog)
2. [Commenti nel codice](#2-commenti-nel-codice)
3. [README.md](#3-readmemd)
4. [Valori da calibrare a robot finito](#4-valori-da-calibrare-a-robot-finito)
5. [Formattazione dei file Markdown](#5-formattazione-dei-file-markdown)
6. [Gestione delle branch](#6-gestione-delle-branch)
7. [Commit](#7-commit)
---

## 1. Changelog

Il changelog **deve essere mantenuto aggiornato** a ogni modifica del codice.

### Formato della voce

```
[DATA] [TITOLO] – [CLASSIFICAZIONE]
Descrizione della modifica o delle modifiche
Firma
```

| Campo | Descrizione |
|---|---|
| **Data** | Data in cui è stata effettuata la modifica |
| **Titolo** | Titolo breve della modifica o delle modifiche |
| **Classificazione** | `modifica` oppure `aggiunta` |
| **Descrizione** | Spiegazione di cosa è stato cambiato o aggiunto e perché |
| **Firma** | Nome di chi ha effettuato la modifica |

### Segnalazioni particolari

Nella voce di changelog va evidenziato, se presente:

- la **creazione di nuovi file**;
- la modifica di un **file importante**, ad esempio `drivetrain` o `robothardware`.
### Esempio

```
22/11/2026 Aggiunta modalità precisione al drivetrain – aggiunta
Aggiunto in Drivetrain.java un metodo per ridurre la velocità dei motori
quando viene premuto il grilletto sinistro. [FILE IMPORTANTE: Drivetrain.java]
Firma: Mario
```
 
---

## 2. Commenti nel codice

Il codice **deve essere commentato** in modo che chiunque nel team possa leggerlo, capirlo e modificarlo senza dover chiedere a chi l'ha scritto.
 
---

## 3. README.md

Il file `README.md` **deve essere sempre aggiornato** e contenere:

- la descrizione delle diverse classi e dei diversi file del progetto;
- una breve descrizione delle funzioni contenute in ciascuno.
  Ogni volta che si aggiunge o si modifica una classe o una funzione, il README va aggiornato di conseguenza.

---

## 4. Valori da calibrare a robot finito

Alcuni valori non possono essere definitivi finché il robot non è completato, ad esempio:

- coefficienti di correzione;
- mappature particolari dei controller.
  Per questi valori è obbligatorio:

1. **segnalarli nel README.md**;
2. **inserirli anche nel manuale tecnico** presente sull'app.
---

## 5. Formattazione dei file Markdown

Perché i file `.md` siano leggibili, è consigliato (e perfettamente lecito) usare un'**IA** per generare e sistemare la formattazione Markdown. I contenuti devono comunque essere verificati da chi li scrive.
 
---

## 6. Gestione delle branch

- La branch principale è **`master`**.
- In caso di **dubbi sulla funzionalità del codice**, o se non si è certi che funzioni, **non pushare su `master`**: pushare su un'altra branch.
- Durante gli incontri del team si verifica insieme se il codice funziona. Se funziona, viene **unito (merge) in `master`**.
### Flusso consigliato

```
modifica / codice dubbio
        │
        ▼
  push su branch separata
        │
        ▼
  verifica agli incontri
        │
   ┌────┴────┐
funziona   non funziona
   │          │
   ▼          ▼
merge in   correzione sulla
 master      branch
```
 
---

## 7. Commit

I messaggi di commit devono permettere a chiunque di capire cosa è stato fatto senza aprire il codice.

### Regole

- Ogni commit contiene **una sola modifica logica**: non mescolare cambiamenti non collegati tra loro.
- Il messaggio è scritto **in italiano**.
- Il titolo usa la stessa **classificazione** del changelog (`modifica` oppure `aggiunta`) e, quando possibile, lo stesso titolo della voce di changelog.
- Se il codice non è sicuro che funzioni, va scritto nel messaggio e il commit va fatto su una **branch separata** (vedi sezione 6).
- Il **changelog va aggiornato nello stesso commit** della modifica.
- La firma è l'autore del commit: ognuno deve avere configurato il proprio nome in git (`git config user.name "Nome"`).
- Evitare messaggi generici come `fix`, `update`, `prova`, `modifiche`.

### Formato

```
[classificazione] Descrizione breve della modifica
```

### Esempi

```
[aggiunta] Modalità precisione al drivetrain
```

```
[modifica] Corretta direzione dei motori posteriori
```

```
[aggiunta] Nuovo file Intake.java per il controllo dell'intake
```

---

## Riepilogo rapido

- [ ] Ho aggiornato il **changelog** (con data, titolo, classificazione, descrizione e firma)?
- [ ] Ho **commentato** il codice?
- [ ] Ho aggiornato il **README.md**?
- [ ] Se ci sono valori da calibrare, li ho segnalati nel **README** e nel **manuale tecnico**?
- [ ] Se non sono sicuro che funzioni, ho pushato su una **branch separata** invece che su `master`?
- [ ] Il messaggio di commit segue il **formato** della sezione 7 ed è nello stesso commit del changelog?


## 8. Formato dei file .md (istruzioni per l'IA)

Questa sezione serve per ottenere file `.md` coerenti **senza dover dare all'IA l'intero file ogni volta**: basta passarle le istruzioni di formato qui sotto, insieme alle informazioni nuove.

### Come si usa

1. Copiare le **regole generali** e il **formato del file** da aggiornare (README, CHANGELOG o RULES).
2. Sotto, scrivere in forma libera cosa è cambiato (nuova classe, nuove funzioni, valore da calibrare, modifica fatta...).
3. L'IA risponde solo con il **blocco pronto da incollare**, indicando in quale sezione va inserito.
4. Controllare il risultato e incollarlo nel file.
### Regole generali (da copiare sempre)

````text
Scrivi in italiano, con tono tecnico e semplice e frasi brevi.
Restituisci SOLO il blocco Markdown nuovo o modificato, pronto da incollare,
e indica in una riga in quale sezione va inserito. Non riscrivere il resto del file.
Non inventare informazioni: se manca qualcosa (valore, funzione, nome), scrivi "Da completare".
 
Formattazione:
- Nomi di file, classi, funzioni e variabili tra backtick.
- Titoli principali con ## numerati, sottotitoli con ###, sezioni separate da ---.
- Riga vuota prima e dopo titoli, tabelle, elenchi e blocchi di codice.
- Tabelle per funzioni e valori; elenchi puntati solo per liste brevi.
- Note e avvisi in citazione (>).
- Parti non ancora scritte: *Da completare.*
- Niente emoji e niente testo decorativo.
````

### Formato di README.md

````text
Il README ha queste sezioni, in quest'ordine:
- Titolo "# Codice robot FTC", descrizione di 2 righe e citazione con i link a RULES.md e CHANGELOG.md.
- "## Indice" numerato con link alle sezioni.
- "## 1. Struttura del progetto": albero dei package e dei file in un blocco di codice, poi la riga
  "File importanti (segnalarli sempre nel changelog quando vengono modificati): ...".
- Un "##" numerato per ogni package. Per ogni file: "### `Nome.java`", una riga di descrizione
  e una tabella con colonne | Funzione | Descrizione |.
- "## Mappatura controller": una tabella per gamepad con colonne | Comando | Funzione | Stato |
  (Stato = Implementato oppure Da completare).
- "## Valori da calibrare a robot finito": tabella con colonne | Valore | File | Stato attuale | Note |.
 
Quando aggiorni:
- Nuova classe: nuovo "###" nel package giusto e aggiunta all'albero della struttura.
- Nuova funzione: nuova riga nella tabella del file, con una sola frase di descrizione.
- Valore o mappatura da sistemare a robot finito: nuova riga nella tabella dedicata e promemoria
  che va inserito anche nel manuale tecnico sull'app.
- Parte non ancora implementata: scrivi "*Da completare.*".
````

### Formato di CHANGELOG.md

````text
Il changelog ha il titolo "# Changelog" e una riga di introduzione con il link a RULES.md.
Poi ci sono le date, dalla più recente alla più vecchia. Per ogni data una sezione "## GG/MM/AAAA"
e, sotto, una voce per ogni modifica con questo formato:
 
### Titolo breve – classificazione
**NUOVO FILE: `Nome.java`**
**FILE IMPORTANTE: `Nome.java`**
 
Descrizione di cosa è stato cambiato e perché, in 1-3 frasi.
 
Firma: Nome
 
Regole:
- Classificazione: solo "modifica" oppure "aggiunta".
- Le righe NUOVO FILE e FILE IMPORTANTE compaiono solo quando servono.
- Una voce per ogni modifica; più modifiche nello stesso giorno stanno sotto la stessa data.
- Se la firma non è nota scrivi "Firma: _______".
````

### Formato di RULES.md

````text
Il file ha un indice numerato in alto e una sezione "## N. Titolo" per ogni argomento,
separate da ---.
- Le regole sono elenchi puntati, con le parole chiave in grassetto.
- Gli esempi stanno in blocchi di codice.
- I campi con più attributi stanno in tabella.
- Ogni nuova regola va aggiunta anche all'indice e al "Riepilogo rapido" finale,
  come domanda nel formato "- [ ] Ho ...?".
- La sezione sul formato dei file .md resta sempre l'ultima.
````

### Esempio di richiesta all'IA

````text
[regole generali + formato del README]
 
Aggiorna il README. Nuova classe Intake.java nel package Movmts: start() avvia l'intake
a potenza 1, stop() lo ferma. Valore da calibrare: INTAKE_POWER, ora 1.0.
````
 