# TP 6 : Simuler un conflit de réservation concurrent avec l'optimistic locking (`@Version`)

> Ce README est volontairement **en dehors** du projet Maven (`reservation-locking/`).
> Les captures se placent dans le dossier `captures/` à côté de ce fichier.

## 1. Structure du rendu

```
tp6-optimistic-locking/
├── README.md                      <- ce fichier
├── captures/                      <- vos captures d'écran
└── reservation-locking/           <- le projet Maven
    ├── pom.xml
    ├── docker-compose.yml         <- serveur H2 pour DBeaver
    ├── docker/h2/Dockerfile       <- image H2 1.4.200 (téléchargée depuis Maven Central)
    └── src/main/
        ├── java/com/example/
        │   ├── model/RoomBooking.java
        │   ├── service/BookingService.java
        │   ├── service/BookingServiceJpa.java
        │   ├── util/JpaBootstrap.java
        │   ├── BookingConflictSimulation.java
        │   ├── BookingRetryExecutor.java
        │   └── BookingRetrySimulation.java
        └── resources/META-INF/persistence.xml
```

## 2. Renommage des classes par rapport à l'énoncé

| Énoncé                          | Ce projet                  |
|---------------------------------|----------------------------|
| `Reservation`                   | `RoomBooking`              |
| `ReservationService`            | `BookingService`           |
| `ReservationServiceImpl`        | `BookingServiceJpa`        |
| `ConcurrentReservationSimulator`| `BookingConflictSimulation`|
| `ReservationRetryHandler`       | `BookingRetryExecutor`     |
| `ConcurrentReservationWithRetry`| `BookingRetrySimulation`   |
| (nouveau)                       | `JpaBootstrap`             |

Champs : `salle` → `roomName`, `dateDebut` → `startTime`, `dateFin` → `endTime`. Table : `room_bookings`.
Persistence units : `bookingH2PU` (H2 en mémoire) et `bookingH2ServerPU` (H2 serveur TCP).

## 3. Deux corrections par rapport au code de l'énoncé

1. **`update` utilise `em.merge()`** au lieu de `find()` + copie des champs. Avec la version de l'énoncé, `find()` recharge la version *courante* de la base : aucun conflit n'est détecté et la transaction 2 réussit. Avec `merge()`, Hibernate compare la version de l'objet détaché (obsolète) à celle de la base et lève bien l'`OptimisticLockException`.
2. **Le retry réapplique la modification** (`Consumer<RoomBooking>`) après rechargement. Dans l'énoncé, `setSalle(getSalle())` ne change rien : la modification voulue serait perdue.

## 4. Prérequis

- Java 8 ou supérieur
- Maven 3.6+
- Docker + Docker Compose (uniquement pour voir la base dans DBeaver)
- DBeaver

## 5. Exécution

### Mode 1 : H2 en mémoire (par défaut, comme l'énoncé)

Lancez simplement `BookingConflictSimulation` puis `BookingRetrySimulation` depuis l'IDE, ou en ligne de commande depuis `reservation-locking/` :

```bash
mvn compile exec:java -Dexec.mainClass=com.example.BookingConflictSimulation
mvn compile exec:java -Dexec.mainClass=com.example.BookingRetrySimulation
```

> La base disparaît à la fin du programme et n'est pas visible dans DBeaver.

### Mode 2 : H2 en serveur TCP (visible dans DBeaver)

1. Démarrer le serveur H2 dans Docker (depuis `reservation-locking/`) :

```powershell
docker compose up -d --build
docker ps
```

La première fois, Docker construit l'image (téléchargement du jar H2 depuis Maven Central). `docker ps` doit afficher `booking-locking-h2` à l'état `Up` avec `0.0.0.0:9092->1521/tcp`.

2. Lancer le programme avec l'option `-Ddb=server` :

- **IntelliJ** : *Run > Edit Configurations*, sélectionner la classe, puis dans **VM options** saisir `-Ddb=server` (avec le tiret). La console doit afficher `>>> Base utilisée : server (persistence unit : bookingH2ServerPU)`.
- **Ligne de commande (PowerShell)** :

```powershell
mvn compile exec:java "-Dexec.mainClass=com.example.BookingConflictSimulation" "-Ddb=server"
mvn compile exec:java "-Dexec.mainClass=com.example.BookingRetrySimulation" "-Ddb=server"
```

Arrêt : `docker compose down` (ajouter `-v` pour supprimer les données).

> **Pourquoi le port 9092 ?** Le port interne du conteneur est 1521, mais il est publié sur **9092** côté PC car le port 1521 est souvent déjà utilisé par Oracle Database (`tnslsnr.exe`). Si 9092 est aussi pris, changez le numéro à gauche dans `docker-compose.yml` (`"9093:1521"`) ainsi que dans `persistence.xml` et DBeaver.

## 6. Connexion DBeaver

1. *Nouvelle connexion* > choisir **H2 Server** (pas « Embedded »).
2. Paramètres :

| Champ    | Valeur      |
|----------|-------------|
| Host     | `localhost` |
| Port     | `9092`      |
| Database | `bookingdb` |
| Username | `sa`        |
| Password | (vide)      |

URL JDBC équivalente : `jdbc:h2:tcp://localhost:9092/bookingdb`

3. **Important : version du driver.** Le serveur et le projet utilisent H2 **1.4.200**. DBeaver propose par défaut H2 2.x, incompatible. Dans la fenêtre de connexion :
   - cliquer sur **Editer le pilote** > onglet **Bibliothèques**
   - sélectionner `com.h2database:h2:RELEASE [2.x]` puis **Supprimer**
   - **Ajouter un Artefact** et saisir `com.h2database:h2:1.4.200`
   - **Télécharger/Mettre à jour**, puis **OK**
4. *Test de la connexion*, puis *Terminer*. La table se trouve dans `BOOKINGDB > PUBLIC > ROOM_BOOKINGS` (elle n'existe qu'après le premier lancement d'une simulation en mode serveur). Rafraîchir avec F5.
5. Requête utile :

```sql
SELECT id, room_name, start_time, end_time, version FROM room_bookings ORDER BY id;
```

La console web H2 est aussi disponible sur http://localhost:8181 (URL JDBC `jdbc:h2:tcp://localhost:9092/bookingdb`, utilisateur `sa`).

## 7. Captures à fournir

### 7.1 Démarrage du conteneur H2
Commande : `docker compose up -d --build` puis `docker ps`

<img width="1510" height="674" alt="Capture d&#39;écran 2026-10-21 164306" src="https://github.com/user-attachments/assets/7bddaadf-9826-4b26-b3a3-9583dcd7eb77" />

### 7.2 Connexion DBeaver réussie

<img width="438" height="439" alt="Capture d&#39;écran 2026-10-21 165819" src="https://github.com/user-attachments/assets/408a51a8-f57d-47bf-b5ac-c8c15e963595" />

### 7.3 Simulation du conflit (`BookingConflictSimulation`)
Console montrant : transaction 1 réussie, transaction 2 en `OptimisticLockException`, requête `update ... where id=? and version=?`.

<img width="1668" height="751" alt="Capture d&#39;écran 2026-10-21 172638" src="https://github.com/user-attachments/assets/beafc2d0-ca49-4364-aaad-fab2da250f54" />

### 7.4 Table `room_bookings` après le conflit
Vous devez voir `room_name = Salle B` et `version = 1` (la modification « Salle C » a été rejetée).

<img width="784" height="340" alt="image" src="https://github.com/user-attachments/assets/e1cd0b7b-1868-451e-9d1a-916bb64c39fd" />

### 7.5 Simulation avec retry (`BookingRetrySimulation`)
Console montrant : « Conflit détecté (tentative 1/3) ».

<img width="881" height="741" alt="image" src="https://github.com/user-attachments/assets/b1097363-4c49-4241-9e5d-18fa97034d4d" />

### 7.6 Table `room_bookings` après le retry
Vous devez voir `room_name = Salle C` et `version = 2` sur la ligne créée par cette exécution (chaque exécution crée une nouvelle ligne).

<img width="1227" height="795" alt="Capture d&#39;écran 2026-10-21 170616" src="https://github.com/user-attachments/assets/6963b58f-83f5-48ef-853a-5e2675867291" />

## 8. Résultat attendu

Conflit :
```
Transaction 1 : modification réussie. -> RoomBooking{id=1, room='Salle B', version=1}
Transaction 2 : conflit détecté - Row was updated or deleted by another transaction ...
État final en base : RoomBooking{id=1, room='Salle B', version=1}
```

> Dans IntelliJ, le message de la transaction 2 (écrit sur `System.err`) peut apparaître après « État final » : c'est un simple décalage d'affichage entre `stdout` et `stderr`.

Retry :
```
Conflit détecté (tentative 1/3)
Transaction 2 : modification réussie après retry. -> RoomBooking{id=1, room='Salle C', version=2}
```
<img width="1480" height="798" alt="Capture d&#39;écran 2026-10-21 170401" src="https://github.com/user-attachments/assets/b6710619-b3a9-47d2-9926-a010f14901d7" />

## 9. Principe en bref

`@Version` ajoute une colonne `version` incrémentée à chaque `UPDATE`. Hibernate émet `UPDATE ... WHERE id=? AND version=?`. Si 0 ligne est modifiée, quelqu'un est passé avant : `OptimisticLockException`. Pas de verrou en base, donc bonne scalabilité, mais les conflits sont détectés après coup et doivent être gérés (retry ou message à l'utilisateur).
