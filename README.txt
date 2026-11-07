The Knife - build e avvio

Requisiti:
- JDK 21 o successivo
- Maven 3.9 o successivo
- PostgreSQL 16 con PostGIS

Il progetto è un reactor Maven composto dai moduli:
- common: DTO, eccezioni e contratto RMI condiviso
- server: servizi, DAO, implementazione RMI e migrazioni
- client: applicazione JavaFX, FXML, CSS e immagini

Dalla radice del repository eseguire:
    mvn clean package

Il build produce:
    bin/theknife-server.jar
    bin/theknife-client.jar

Configurare .env copiando .env.example e avviare il database:
    docker compose up -d db

Avviare prima il server, che applica le migrazioni e attende connessioni RMI:
    java -jar bin/theknife-server.jar

In un altro terminale avviare il client:
    java -jar bin/theknife-client.jar

Il database PostgreSQL usa la porta host DB_HOST_PORT (5432 per default).
Il server usa RMI sulla porta 1099 per default.
