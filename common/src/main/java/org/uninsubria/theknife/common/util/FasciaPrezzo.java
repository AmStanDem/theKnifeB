package org.uninsubria.theknife.common.util;

import java.util.Currency;
import java.util.Locale;

/**
 * Utility per normalizzare e confrontare fasce di prezzo simboliche.
 */
public final class FasciaPrezzo {
    public static final String QUALSIASI = "*";

    private FasciaPrezzo() {
    }

    public static String normalizza(String fascia) {
        if (fascia == null || fascia.isBlank()) {
            return null;
        }
        String valore = fascia.trim();
        if (QUALSIASI.equals(valore)) {
            return QUALSIASI;
        }
        int simbolo = -1;
        int quantita = 0;
        for (int codePoint : valore.codePoints().toArray()) {
            if (Character.getType(codePoint) != Character.CURRENCY_SYMBOL
                    || (simbolo != -1 && simbolo != codePoint)) {
                return null;
            }
            simbolo = codePoint;
            quantita++;
        }
        return quantita >= 1 && quantita <= 4 ? valore : null;
    }

    public static int livello(String fascia) {
        String normalizzata = normalizza(fascia);
        return normalizzata == null || QUALSIASI.equals(normalizzata)
                ? 0 : normalizzata.codePointCount(0, normalizzata.length());
    }

    public static String simboli(String simbolo, int livello) {
        if (simbolo == null || simbolo.codePointCount(0, simbolo.length()) != 1
                || Character.getType(simbolo.codePointAt(0)) != Character.CURRENCY_SYMBOL
                || livello < 1 || livello > 4) {
            throw new IllegalArgumentException("Simbolo o livello di prezzo non valido.");
        }
        return simbolo.repeat(livello);
    }

    public static String simboloValuta(String countryCode) {
        if (countryCode == null || !countryCode.matches("(?i)[a-z]{2}")) {
            return "¤";
        }
        try {
            Currency valuta = Currency.getInstance(new Locale("", countryCode.toUpperCase(Locale.ROOT)));
            String simbolo = valuta.getSymbol(new Locale("", countryCode.toUpperCase(Locale.ROOT)));
            return simbolo.codePointCount(0, simbolo.length()) == 1
                    && Character.getType(simbolo.codePointAt(0)) == Character.CURRENCY_SYMBOL
                    ? simbolo : "¤";
        } catch (IllegalArgumentException e) {
            return "¤";
        }
    }
}


/* TODO: completare l'implementazione della fascia di prezzo modificando il simbolo generico ¤ e i simiboli nei
     tag nella lista ristoranti e modificare i il contenuto del database con ristoranti con una fascia
     di prezzo simbolica , potremmo anche tenere i vecchi valori numerici ma li dovremmo comunque associare
     alla valuta il che porterebbe al conflitto tra stati con la stesso simbolo  ma con valuta e tenore di vita
     diverso come per il dollaro americano e canadese.
*  */
