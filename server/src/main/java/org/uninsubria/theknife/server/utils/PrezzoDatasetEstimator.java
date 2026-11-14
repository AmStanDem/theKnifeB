/**
 * Stima in euro un prezzo medio a partire dalla fascia relativa presente nel dataset Michelin.
 * La stima è euristica: usa la fascia (numero di simboli) e un coefficiente economico per Paese.
 */
package org.uninsubria.theknife.server.utils;

import java.util.Locale;
import java.util.Set;

public final class PrezzoDatasetEstimator {
    private static final Set<String> LOW_COST_COUNTRIES = Set.of(
            "bangladesh", "cambodia", "india", "indonesia", "laos", "nepal",
            "pakistan", "sri lanka", "thailand", "vietnam", "viet nam");
    private static final Set<String> LOWER_MIDDLE_COST_COUNTRIES = Set.of(
            "brazil", "colombia", "malaysia", "mexico", "philippines", "turkey", "türkiye");
    private static final Set<String> MODERATE_COST_COUNTRIES = Set.of(
            "greece", "italy", "japan", "poland", "portugal", "south korea",
            "spain", "taiwan");
    private static final Set<String> HIGH_COST_COUNTRIES = Set.of(
            "austria", "belgium", "canada", "france", "germany", "ireland",
            "netherlands", "united kingdom", "united states", "usa");
    private static final Set<String> VERY_HIGH_COST_COUNTRIES = Set.of(
            "denmark", "iceland", "luxembourg", "norway", "switzerland");

    private PrezzoDatasetEstimator() {
    }

    public static Double stimaInEuro(String priceLevel, String country, String restaurantName, String address) {
        int tier = tier(priceLevel);
        if (tier == 0) {
            return null;
        }

        double lower = switch (tier) {
            case 1 -> 12.0;
            case 2 -> 25.0;
            case 3 -> 50.0;
            case 4 -> 80.0;
            default -> throw new IllegalStateException("Fascia prezzo non supportata.");
        };
        double upper = switch (tier) {
            case 1 -> 25.0;
            case 2 -> 50.0;
            case 3 -> 80.0;
            case 4 -> 160.0;
            default -> throw new IllegalStateException("Fascia prezzo non supportata.");
        };

        String normalizedCountry = country == null ? "" : country.trim().toLowerCase(Locale.ROOT);
        double countryFactor = countryFactor(normalizedCountry);
        String identity = (restaurantName == null ? "" : restaurantName)
                + "|" + (address == null ? "" : address)
                + "|" + normalizedCountry;
        double variation = Math.floorMod(identity.hashCode(), 1_000) / 999.0;
        double positionInTier = 0.35 + variation * 0.30;
        return Math.round((lower + (upper - lower) * positionInTier) * countryFactor * 100.0) / 100.0;
    }

    private static int tier(String priceLevel) {
        if (priceLevel == null || priceLevel.isBlank()) {
            return 0;
        }

        int symbol = -1;
        int count = 0;
        for (int codePoint : priceLevel.codePoints().toArray()) {
            if (Character.isWhitespace(codePoint)) {
                continue;
            }
            if (Character.getType(codePoint) != Character.CURRENCY_SYMBOL
                    || (symbol != -1 && symbol != codePoint)) {
                return 0;
            }
            symbol = codePoint;
            count++;
        }
        return count >= 1 && count <= 4 ? count : 0;
    }

    private static double countryFactor(String country) {
        if (VERY_HIGH_COST_COUNTRIES.contains(country)) {
            return 1.45;
        }
        if (HIGH_COST_COUNTRIES.contains(country)) {
            return 1.20;
        }
        if (MODERATE_COST_COUNTRIES.contains(country)) {
            return 0.95;
        }
        if (LOWER_MIDDLE_COST_COUNTRIES.contains(country)) {
            return 0.75;
        }
        if (LOW_COST_COUNTRIES.contains(country)) {
            return 0.55;
        }
        return 1.0;
    }
}
