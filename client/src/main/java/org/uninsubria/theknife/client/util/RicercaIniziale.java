/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.client.util;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public final class RicercaIniziale {

    public static final double RAGGIO_RICERCA_KM = 25.0;

    public record Criteri(
            String testo,
            String localita,
            Double latitudine,
            Double longitudine,
            List<String> tipologieCucina,
            Double prezzoMin,
            Double prezzoMax,
            Double mediaStelleMinima,
            boolean delivery,
            boolean bookingOnline
    ) {
        public Criteri {
            tipologieCucina = tipologieCucina == null ? List.of() : List.copyOf(tipologieCucina);
        }
    }

    private static final AtomicReference<Criteri> CRITERI = new AtomicReference<>();

    private RicercaIniziale() {
    }

    public static void imposta(String testo, GeocodingService.Luogo luogo) {
        imposta(testo, luogo, List.of(), null, null, null, false, false);
    }

    public static void imposta(
            String testo,
            GeocodingService.Luogo luogo,
            List<String> tipologieCucina,
            Double prezzoMin,
            Double prezzoMax,
            Double mediaStelleMinima,
            boolean delivery,
            boolean bookingOnline
    ) {
        CRITERI.set(new Criteri(
                testo,
                luogo == null ? null : luogo.localita(),
                luogo == null ? null : luogo.latitudine(),
                luogo == null ? null : luogo.longitudine(),
                tipologieCucina,
                prezzoMin,
                prezzoMax,
                mediaStelleMinima,
                delivery,
                bookingOnline
        ));
    }

    public static Criteri consuma() {
        return CRITERI.getAndSet(null);
    }
}
