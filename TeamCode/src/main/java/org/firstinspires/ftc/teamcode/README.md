# Codice robot FTC

Codice del robot per la stagione FTC di quest'anno.
Il progetto usa l'odometria **goBILDA Pinpoint** per la localizzazione e una trasmissione **mecanum** a 4 motori.

> Prima di modificare qualsiasi cosa leggi **[RULES.md](RULES.md)**: contiene le regole del team (changelog, commenti, branch).
> Ogni modifica va registrata in **[CHANGELOG.md](CHANGELOG.md)**.

## Indice

1. [Struttura del progetto](#1-struttura-del-progetto)
2. [Package `Autonomus`](#2-package-autonomus)
3. [Package `Movmts`](#3-package-movmts)
4. [Package `Robothardware`](#4-package-robothardware)
5. [Package `General`](#5-package-general)
6. [Package `Opmodes`](#6-package-opmodespecific)
7. [Mappatura controller](#5-mappatura-controller)
8. [Valori da calibrare a robot finito](#6-valori-da-calibrare-a-robot-finito)

---

## 1. Struttura del progetto

```
org.firstinspires.ftc.teamcode
├── autonomus/            da completare
├── Movmts/               movimento manuale (teleop)
│   └── StandardMvmnt.java
└── RobotHardware/        hardware del robot
    └── RobotHardware.java
```

File importanti (segnalarli sempre nel changelog quando vengono modificati): `RobotHardware.java`, `StandardMvmnt.java`.

---

## 2. Package `Autonomus`

*Da completare.*

---

## 3. Package `Movmts`

### `StandardMvmnt.java`
Movimento manuale mecanum da gamepad.

| Funzione | Descrizione |
|---|---|
| `move()` | Legge stick e bumper, applica le zone morte, calcola le potenze e le assegna ai 4 motori moltiplicate per la velocità corrente. |
| `speedControl(left_bumper, left_trigger)` | Gestisce la velocità: bumper sinistro = massima (1), grilletto sinistro = frenata proporzionale, altrimenti velocità standard `SPEED`. |
| `MotorOut(lX, lY, rX)` | Calcola le potenze dei 4 motori (avanti/laterale/rotazione) normalizzandole in modo che nessuna superi 1. |

---

## 4. Package `Robothardware`

### `Hardware.java`

Dichiara e inizializza l'hardware del robot.

| Funzione | Descrizione |
|---|---|
| `Hardware()` | Costruttore vuoto della classe. |
| `Hardware(hwmp, telemetry)` | Configura il Pinpoint (risoluzione, offset, direzioni encoder, reset posizione e IMU con robot fermo e attesa fino a 3 s dello stato `READY`), poi recupera i 4 motori di movimento, imposta direzioni, modalità `RUN_WITHOUT_ENCODER` e freno a potenza zero (`BRAKE`). |

Hardware dichiarato: `lfD`, `lbD`, `rfD`, `rbD` (motori di movimento) e `pinpoint`.

> Durante l'inizializzazione di `Hardware` il robot **deve rimanere fermo**: l'IMU del Pinpoint si calibra in quel momento.

### `Vision.java`

Gestisce la telecamera `Limelight3A` per il rilevamento del bersaglio e la stima della distanza.

| Funzione | Descrizione |
|---|---|
| `Vision(hw)` | Recupera la `Limelight3A` (`"limelight"`), imposta il poll rate a 100 Hz e seleziona la pipeline `1` (`1` = rosso, `2` = blu, `3` = giallo). |
| `start()` | Avvia la Limelight se presente. |
| `stop()` | Ferma la Limelight se presente. |
| `pipeLineSwitch(i)` | Cambia la pipeline attiva della Limelight e aggiorna `currentPipeLine`. |
| `computeDistance(tyDeg)` | Calcola la distanza dal bersaglio usando `ALTEZZA_CAMERA`, `ALTEZZA_PALLINA`, `ANGOLO_CAMERA` e `tyDeg` (restituisce `NaN` se l'angolo risultante è `<= 0.01` rad). |
| `update()` | Legge l'ultimo risultato (`LLResult`) dalla Limelight e aggiorna `valid`, `tx`, `ty`, `ta` e `distance`. |
| `isAvailable()` | Restituisce `true` se il risultato è valido e la distanza calcolata non è `NaN`. |
| `getTx()` | Restituisce l'offset orizzontale (`tx`) del bersaglio. |
| `getTy()` | Restituisce l'offset verticale (`ty`) del bersaglio. |
| `getTa()` | Restituisce l'area (`ta`) del bersaglio rilevato. |
| `getDistance()` | Restituisce la distanza stimata dal bersaglio. |

---

## 5. Package `General`

### `Robot.java`

Centralizza l'inizializzazione e la gestione dei sottosistemi del robot (`Hardware`, `Vision` e `StandardMvmnt`).

| Funzione | Descrizione |
|---|---|
| `Robot(hw, telemetry)` | Costruttore che inizializza `hardware` e `vision` passando `HardwareMap` e `Telemetry` (istanzia anche `standardMvmnt`). |
| `Start()` | Avvia il sottosistema di visione richiamando `vision.start()`. |
| `Update()` | Aggiorna i dati della visione richiamando `vision.update()`. |
| `Stop()` | Ferma il sottosistema di visione richiamando `vision.stop()`. |

---

## 6. Package `Opmodes`

### `MainTeleOp.java`

OpMode principale in modalità TeleOp (`@TeleOp(name = "TeleOp - FTC", group = "OpModes")`).

| Funzione | Descrizione |
|---|---|
| `runOpMode()` | Inizializza `Robot` gestendo eventuali errori in telemetria, avvia `robot.Start()` dopo `waitForStart()` e nel ciclo attivo aggiorna `robot.Update()`, controlla la guida con `gamepad1`, imposta la pipeline `1` della visione e mostra in telemetria il colore associato alla pipeline attiva. |

---

## 7. Mappatura controller

*Da completare.* La mappatura definitiva verrà fissata a robot finito (vedi anche [sezione 6](#6-valori-da-calibrare-a-robot-finito)). Quando un comando viene assegnato o cambiato, aggiornare questa tabella e il manuale tecnico sull'app.

### Gamepad 1 – Guida

| Comando | Funzione | Stato |
|---|---|---|
| Stick sinistro | Movimento (avanti/indietro e laterale) | Implementato |
| Stick destro (asse X) | Rotazione | Implementato |
| Bumper sinistro (L1) | Velocità massima | Implementato |
| Grilletto sinistro (L2) | Frenata proporzionale | Implementato |
| *da definire* | *da definire* | Da completare |

### Gamepad 2 – Meccanismi

| Comando | Funzione | Stato |
|---|---|---|
| *da definire* | *da definire* | Da completare |

---

## 8. Valori da calibrare a robot finito

Questi valori vanno **tarati sul robot finito**. Una volta definitivi vanno riportati anche nel **manuale tecnico sull'app** (vedi RULES.md, sezione 4).

| Valore | File | Stato attuale | Note |
|---|---|---|---|
| Offset Pinpoint | `Hardware.java` | `-102.5` mm / `-57.5` mm | Da verificare dopo il montaggio. |
| Direzioni encoder Pinpoint | `Hardware.java` | forward `FORWARD`, strafe `REVERSED` | Da verificare dopo il montaggio. |
| Direzioni motori di movimento | `Hardware.java` | `lf`, `lb` `REVERSE`; `rf`, `rb` `FORWARD` | Verificare con il robot montato. |
| `ALTEZZA_CAMERA` | `Vision.java` | `29` | Altezza della Limelight da terra; da verificare dopo il montaggio. |
| `ANGOLO_CAMERA` | `Vision.java` | `21.2` | Inclinazione della Limelight in gradi; da verificare dopo il montaggio. |
| `ALTEZZA_PALLINA` | `Vision.java` | `0` | Altezza del bersaglio; da verificare sull'elemento di gioco. |
| `SPEED` | `StandardMvmnt.java` | `0.5` | Velocità standard del teleop. |
| Zone morte stick | `StandardMvmnt.java` | `0.05` (`lX`, `lY`, `rX`) | Mappatura controller da rivedere con i piloti. Verificare anche il segno di `lY` (nel gamepad FTC lo stick in avanti dà valori negativi). |