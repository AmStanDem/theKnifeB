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

La build genera il server:
    bin/theknife-server.jar

e i client JavaFX con le librerie native specifiche della piattaforma:
    bin/theknife-client-linux.jar
    bin/theknife-client-windows.jar
    bin/theknife-client-macos.jar

Distribuire a ogni compagno il JAR corrispondente al suo sistema operativo.

Le viste di dettaglio, ricerca, ristoranti del gestore, preferiti e recensioni
personali ricevono aggiornamenti tramite callback RMI. Le notifiche invalidano i
dati locali, che vengono ricaricati dal server; le sottoscrizioni terminano quando
si chiude la vista. Non viene eseguito polling periodico.

Configurare .env copiando .env.example e avviare il database:
    docker compose up -d db

Per avviare il server da Maven, in un terminale:
    mvn -pl server -am -Prun-server integration-test
Il server resta attivo; per arrestarlo usa Ctrl+C.

Per avviare il client da Maven, in un secondo terminale:
    mvn -pl client -am -Prun-client integration-test

In alternativa, dopo la build, avviare prima il server:
    java -jar bin/theknife-server.jar

In un secondo terminale avviare il client Linux:
    java -jar bin/theknife-client-linux.jar

Su Windows usare invece:
    java -jar bin/theknife-client-windows.jar

Il database PostgreSQL usa la porta host DB_HOST_PORT (5432 per default).
Il server usa RMI sulla porta 1099 per default.
