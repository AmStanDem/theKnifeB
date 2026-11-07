/**
 * Harness d'integrazione concorrente via RMI; richiede un database dedicato.
 */
package org.uninsubria.theknife.server;

import org.uninsubria.theknife.common.dto.RecensioneDTO;
import org.uninsubria.theknife.common.exceptions.AzioneDuplicataException;
import org.uninsubria.theknife.common.exceptions.ModificaObsoletaException;
import org.uninsubria.theknife.common.rmi.ITheKnifeServer;

import java.rmi.registry.LocateRegistry;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

public final class RmiConcurrencyHarness {
    private static final String DB_URL = required("THEKNIFE_TEST_JDBC_URL");
    private static final String DB_USER = required("THEKNIFE_TEST_DB_USER");
    private static final String DB_PASSWORD = required("THEKNIFE_TEST_DB_PASSWORD");

    private RmiConcurrencyHarness() {
    }

    public static void main(String[] args) throws Exception {
        String databaseName = DB_URL.substring(DB_URL.lastIndexOf('/') + 1).split("\\?", 2)[0];
        if (!databaseName.toLowerCase().contains("test")) {
            throw new IllegalStateException("Il database di test deve avere 'test' nel nome.");
        }
        String host = System.getenv().getOrDefault("THEKNIFE_RMI_HOST", "localhost");
        int port = Integer.parseInt(System.getenv().getOrDefault("THEKNIFE_RMI_PORT", "1099"));
        var registry = LocateRegistry.getRegistry(host, port);
        ITheKnifeServer clientA = (ITheKnifeServer) registry.lookup("TheKnifeServer");
        ITheKnifeServer clientB = (ITheKnifeServer) registry.lookup("TheKnifeServer");

        Fixture fixture = createFixture();
        try {
            assertOneSuccessOneDuplicate(runTogether(
                    () -> {
                        clientA.aggiungiPreferito(fixture.customerId(), fixture.restaurantId());
                        return "success";
                    },
                    () -> {
                        clientB.aggiungiPreferito(fixture.customerId(), fixture.restaurantId());
                        return "success";
                    }), "preferiti");
            assertCount("SELECT COUNT(*) FROM Preferiti WHERE id_utente = ? AND id_ristorante = ?",
                    fixture.customerId(), fixture.restaurantId(), 1);

            RecensioneDTO newReview = new RecensioneDTO(
                    null, 4.5, "review-race-" + UUID.randomUUID(), "Harness",
                    LocalDateTime.now(), null, fixture.restaurantId(), fixture.restaurantName());
            assertOneSuccessOneDuplicate(runTogether(
                    () -> {
                        clientA.inserisciRecensione(newReview, fixture.restaurantId(), fixture.customerId());
                        return "success";
                    },
                    () -> {
                        clientB.inserisciRecensione(newReview, fixture.restaurantId(), fixture.customerId());
                        return "success";
                    }), "recensioni");
            assertCount("SELECT COUNT(*) FROM Recensioni WHERE id_utente = ? AND id_ristorante = ?",
                    fixture.customerId(), fixture.restaurantId(), 1);

            int reviewId = findReviewId(fixture.customerId(), fixture.restaurantId());
            List<String> updateResults = runTogether(
                    () -> update(clientA, reviewId, fixture.customerId(), "update-A"),
                    () -> update(clientB, reviewId, fixture.customerId(), "update-B"));
            if (updateResults.stream().filter("success"::equals).count() != 1
                    || updateResults.stream().filter("stale"::equals).count() != 1) {
                throw new AssertionError("Aggiornamento concorrente inatteso: " + updateResults);
            }
            assertVersion(reviewId, 1);
            System.out.println("OK: preferito, recensione duplicata e optimistic locking concorrenti.");
        } finally {
            deleteFixture(fixture);
        }
    }

    private static String update(ITheKnifeServer server, int reviewId, int customerId, String text)
            throws Exception {
        try {
            server.modificaRecensione(reviewId, customerId, text, 4.0, 0);
            return "success";
        } catch (ModificaObsoletaException expected) {
            return "stale";
        }
    }

    private static List<String> runTogether(Callable<String> clientA, Callable<String> clientB)
            throws Exception {
        ExecutorService clients = Executors.newFixedThreadPool(2);
        CyclicBarrier start = new CyclicBarrier(3);
        try {
            Future<String> a = clients.submit(() -> {
                start.await();
                try {
                    return clientA.call();
                } catch (AzioneDuplicataException expected) {
                    return "duplicate";
                }
            });
            Future<String> b = clients.submit(() -> {
                start.await();
                try {
                    return clientB.call();
                } catch (AzioneDuplicataException expected) {
                    return "duplicate";
                }
            });
            start.await();
            return List.of(a.get(30, TimeUnit.SECONDS), b.get(30, TimeUnit.SECONDS));
        } finally {
            clients.shutdownNow();
        }
    }

    private static void assertOneSuccessOneDuplicate(List<String> outcomes, String operation) {
        if (outcomes.stream().filter("success"::equals).count() != 1
                || outcomes.stream().filter("duplicate"::equals).count() != 1) {
            throw new AssertionError(operation + ": attesi un successo e un duplicato, ottenuti " + outcomes);
        }
    }

    private static Fixture createFixture() throws Exception {
        String suffix = UUID.randomUUID().toString();
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            connection.setAutoCommit(false);
            try {
                int managerId = insertUser(connection, "manager-" + suffix, "gestore");
                int customerId = insertUser(connection, "customer-" + suffix, "cliente");
                String restaurantName = "concurrency-" + suffix;
                String restaurantSql = """
                        INSERT INTO RistorantiTheKnife
                            (id_gestore, nome, indirizzo, citta, nazione, latitudine, longitudine, prezzo_medio)
                        VALUES (?, ?, 'Harness Street 1', 'Varese', 'Italia', 45.8, 8.8, 20.00)
                        RETURNING id_ristorante
                        """;
                try (PreparedStatement statement = connection.prepareStatement(restaurantSql)) {
                    statement.setInt(1, managerId);
                    statement.setString(2, restaurantName);
                    try (ResultSet result = statement.executeQuery()) {
                        result.next();
                        Fixture fixture = new Fixture(managerId, customerId, result.getInt(1), restaurantName);
                        connection.commit();
                        return fixture;
                    }
                }
            } catch (Exception e) {
                connection.rollback();
                throw e;
            }
        }
    }

    private static int insertUser(Connection connection, String emailPrefix, String role) throws Exception {
        String sql = """
                INSERT INTO Utenti (email, password, nome, cognome, domicilio, ruolo)
                VALUES (?, 'test-only', 'Concurrency', 'Harness', 'Test', ?)
                RETURNING id_utente
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, emailPrefix + "@example.invalid");
            statement.setString(2, role);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getInt(1);
            }
        }
    }

    private static int findReviewId(int customerId, int restaurantId) throws Exception {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id_recensione FROM Recensioni WHERE id_utente = ? AND id_ristorante = ?")) {
            statement.setInt(1, customerId);
            statement.setInt(2, restaurantId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new AssertionError("Recensione fixture non trovata.");
                }
                return result.getInt(1);
            }
        }
    }

    private static void assertVersion(int reviewId, int expectedVersion) throws Exception {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT versione FROM Recensioni WHERE id_recensione = ?")) {
            statement.setInt(1, reviewId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next() || result.getInt(1) != expectedVersion) {
                    throw new AssertionError("Versione finale non valida per la recensione " + reviewId);
                }
            }
        }
    }

    private static void assertCount(String sql, int firstId, int secondId, int expectedCount) throws Exception {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, firstId);
            statement.setInt(2, secondId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next() || result.getInt(1) != expectedCount) {
                    throw new AssertionError("Conteggio finale inatteso, atteso " + expectedCount);
                }
            }
        }
    }

    private static void deleteFixture(Fixture fixture) throws Exception {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM RistorantiTheKnife WHERE id_ristorante = ?")) {
                statement.setInt(1, fixture.restaurantId());
                statement.executeUpdate();
            }
            try (PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM Utenti WHERE id_utente IN (?, ?)")) {
                statement.setInt(1, fixture.managerId());
                statement.setInt(2, fixture.customerId());
                statement.executeUpdate();
            }
        }
    }

    private static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Variabile d'ambiente obbligatoria: " + name);
        }
        return value;
    }

    private record Fixture(int managerId, int customerId, int restaurantId, String restaurantName) {
    }
}
