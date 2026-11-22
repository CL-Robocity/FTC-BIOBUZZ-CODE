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