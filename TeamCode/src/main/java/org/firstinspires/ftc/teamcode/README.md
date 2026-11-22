# Codice robot FTC

Codice del robot per la stagione FTC di quest'anno.
Il progetto usa l'odometria **goBILDA Pinpoint** per la localizzazione e una trasmissione **mecanum** a 4 motori.

> Prima di modificare qualsiasi cosa leggi **[RULES.md](RULES.md)**: contiene le regole del team (changelog, commenti, branch).
> Ogni modifica va registrata in **[CHANGELOG.md](CHANGELOG.md)**.

## Indice

1. [Struttura del progetto](#1-struttura-del-progetto)
2. [Package `autonomus`](#2-package-autonomus)
3. [Package `Movmts`](#3-package-movmts)
4. [Package `RobotHardware`](#4-package-robothardware)
5. [Mappatura controller](#5-mappatura-controller)
6. [Valori da calibrare a robot finito](#6-valori-da-calibrare-a-robot-finito)

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

## 2. Package `autonomus`

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

## 4. Package `RobotHardware`

### `RobotHardware.java`
Dichiara e inizializza l'hardware del robot.

| Funzione | Descrizione |
|---|---|
| `init(hwmp, telemetry)` | Configura il Pinpoint (risoluzione, offset, direzioni encoder, reset IMU con robot fermo e attesa fino a 3 s dello stato `READY`), poi recupera i 4 motori di movimento, imposta direzioni, modalità `RUN_WITHOUT_ENCODER` e freno a potenza zero. |

Hardware dichiarato: `lfD`, `lbD`, `rfD`, `rbD` (motori di movimento) e `pinpoint`.

> Durante `init` il robot **deve rimanere fermo**: l'IMU del Pinpoint si calibra in quel momento.

---

## 5. Mappatura controller

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

## 6. Valori da calibrare a robot finito

Questi valori vanno **tarati sul robot finito**. Una volta definitivi vanno riportati anche nel **manuale tecnico sull'app** (vedi RULES.md, sezione 4).

| Valore | File | Stato attuale | Note |
|---|---|---|---|
| Offset Pinpoint | `RobotHardware.java` | -102.5 mm / -57.5 mm | Da verificare dopo il montaggio. |
| Direzioni encoder Pinpoint | `RobotHardware.java` | forward `FORWARD`, strafe `REVERSED` | Da verificare dopo il montaggio. |
| Direzioni motori di movimento | `RobotHardware.java` | `lf`, `lb` `REVERSE`; `rf`, `rb` `FORWARD` | Verificare con il robot montato. |
| `SPEED` | `StandardMvmnt.java` | `0.5` | Velocità standard del teleop. |
| Zone morte stick | `StandardMvmnt.java` | sx `0.4`, dx `0.2` | Mappatura controller da rivedere con i piloti. Verificare anche il segno di `lY` (nel gamepad FTC lo stick in avanti dà valori negativi). |