/**
 * Autori: Thomas Riotto (matr. 760981, sede VA), Alessandro Tullo (matr. 760760, sede VA),
 * Giovanni Albé (matr. 760117, sede VA), Antonio Pesavento (matr. 759933, sede VA).
 */
package org.uninsubria.theknife.common.dto;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Parametri di una ricerca paginata di ristoranti.
 *
 * @param localita località geografica da utilizzare nella ricerca
 * @param testo testo libero da cercare tra i dati dei ristoranti
 * @param tipologieCucina tipologie di cucina da includere nella ricerca
 * @param prezzoMin prezzo minimo accettato
 * @param prezzoMax prezzo massimo accettato
 * @param mediaStelleMinima valutazione media minima compresa tra 0 e 5
 * @param offset numero di risultati da saltare
 * @param limite numero massimo di risultati da restituire, compreso tra 1 e 100
 * @param latitudineRiferimento latitudine del punto centrale della ricerca
 * @param longitudineRiferimento longitudine del punto centrale della ricerca
 * @param raggioKm raggio di ricerca in chilometri
 * @param consegna indica se cercare ristoranti con consegna a domicilio
 * @param prenotazioneOnline indica se cercare ristoranti con prenotazione online
 */
public record RicercaRistorantiPaginaDTO(
        String localita,
        String testo,
        List<String> tipologieCucina,
        Double prezzoMin,
        Double prezzoMax,
        Double mediaStelleMinima,
        int offset,
        int limite,
        Double latitudineRiferimento,
        Double longitudineRiferimento,
        Double raggioKm,
        boolean consegna,
        boolean prenotazioneOnline
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Normalizza la località e i tipi di cucina e valida filtri e paginazione.
     *
     * @throws IllegalArgumentException se prezzo, valutazione o paginazione non sono validi,
     *         se mancano coordinate complete o se località, coordinate e raggio
     *         non soddisfano i requisiti della ricerca
     */
    public RicercaRistorantiPaginaDTO {

        localita = localita == null ? "" : localita.trim();
        tipologieCucina = tipologieCucina == null ? List.of() : List.copyOf(tipologieCucina);
        if (prezzoMin != null && prezzoMax != null && prezzoMin > prezzoMax) {
            throw new IllegalArgumentException("Il prezzo minimo non può superare il prezzo massimo.");
        }
        if (mediaStelleMinima != null && (mediaStelleMinima < 0 || mediaStelleMinima > 5)) {
            throw new IllegalArgumentException("La valutazione deve essere compresa tra 0 e 5.");
        }
        if (offset < 0 || limite < 1 || limite > 100) {
            throw new IllegalArgumentException("Paginazione non valida.");
        }
        boolean haLatitudine = latitudineRiferimento != null;
        boolean haLongitudine = longitudineRiferimento != null;
        boolean haRaggio = raggioKm != null;
        if (haLatitudine != haLongitudine || haLatitudine != haRaggio) {
            throw new IllegalArgumentException("Per la ricerca geografica servono coordinate e raggio.");
        }
        if (localita.isBlank() || !haLatitudine) {
            throw new IllegalArgumentException(
                    "Per la ricerca paginata sono obbligatorie località, coordinate e raggio.");
        }
        if (!Double.isFinite(latitudineRiferimento)
                || latitudineRiferimento < -90 || latitudineRiferimento > 90
                || !Double.isFinite(longitudineRiferimento)
                || longitudineRiferimento < -180 || longitudineRiferimento > 180
                || !Double.isFinite(raggioKm) || raggioKm <= 0 || raggioKm > 500) {
            throw new IllegalArgumentException("Coordinate o raggio geografico non validi.");
        }
    }
}
