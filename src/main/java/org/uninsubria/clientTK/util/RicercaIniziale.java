package org.uninsubria.clientTK.util;

import java.util.concurrent.atomic.AtomicReference;

public final class RicercaIniziale {

    public static final double RAGGIO_RICERCA_KM = 25.0;

    public record Criteri(String testo, Double latitudine, Double longitudine) {
    }

    private static final AtomicReference<Criteri> CRITERI = new AtomicReference<>();

    private RicercaIniziale() {
    }

    public static void imposta(String testo, GeocodingService.Luogo luogo) {
        CRITERI.set(new Criteri(
                testo,
                luogo == null ? null : luogo.latitudine(),
                luogo == null ? null : luogo.longitudine()
        ));
    }

    public static Criteri consuma() {
        return CRITERI.getAndSet(null);
    }
}
