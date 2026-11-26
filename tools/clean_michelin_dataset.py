#!/usr/bin/env python3
"""Rigenera il dataset "clean" dei ristoranti Michelin.

Il repository include due file:

* ``server/src/main/resources/dataset/michelin_my_maps.csv``
  copia fedele del dataset originale (usata solo come sorgente);
* ``server/src/main/resources/dataset/michelin_my_maps_clean.csv``
  versione importata a runtime da ``CsvImporter``.

Lo script risolve in modo deterministico e riproducibile i problemi di
qualita' individuati nel dataset:

1. **Telefoni in notazione scientifica.** Il file originale e' passato
   attraverso Excel, che interpreta i numeri di telefono come numeri e li
   riscrive in notazione scientifica troncando le cifre finali
   (es. ``+351939482939`` -> ``3.51939E+11``). Le cifre corrette vengono
   recuperate dallo snapshot sorgente del dataset (stesso URL di guida),
   agganciando per ``Url``. Senza snapshot il numero viene solo espanso
   (best effort, con perdita di precisione).
2. **URL del sito web malformati** (``httpss://``, schema mancante,
   ``http://httpss//``) normalizzati a ``https://``.
3. **Etichette di paese incoerenti**: ``Czech Republic`` -> ``Czechia``.
4. **Paese mancante per le citta-stato**: ``Singapore``, ``Hong Kong``,
   ``Dubai``, ``Abu Dhabi``, ``Macau`` e ``Luxembourg`` non hanno il
   secondo componente nella colonna ``Location``; viene aggiunto il paese
   corretto.
5. **Campi invertiti**: per ``Tokumaru`` la colonna ``Cuisine`` conteneva
   l'indirizzo e il prezzo era assente. Valori corretti (fonte: pagina
   ufficiale della Guida Michelin).

Uso::

    python3 tools/clean_michelin_dataset.py

Lo snapshot di riferimento viene scaricato una sola volta nella cartella
temporanea ed e' individuato dal commit:

    d521a7d8e1ff4b48125c5333f75e8b77d83e70cc  (2025-02-01)

che corrisponde esattamente al dataset originale (17.737 record, 100% di
corrispondenza sugli URL di guida).
"""

from __future__ import annotations

import argparse
import csv
import os
import re
import sys
import tempfile
import urllib.request
from decimal import Decimal

# Commit dello snapshot sorgente identico al dataset originale.
REFERENCE_SHA = "d521a7d8e1ff4b48125c5333f75e8b77d83e70cc"
REFERENCE_URL = (
    "https://raw.githubusercontent.com/ngshiheng/michelin-my-maps/"
    f"{REFERENCE_SHA}/data/michelin_my_maps.csv"
)

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ORIGINAL = os.path.join(
    REPO_ROOT, "server", "src", "main", "resources", "dataset", "michelin_my_maps.csv"
)
CLEAN = os.path.join(
    REPO_ROOT, "server", "src", "main", "resources", "dataset",
    "michelin_my_maps_clean.csv",
)

# Citta-stato a cui manca il paese nella colonna Location del dataset.
CITY_COUNTRY = {
    "singapore": "Singapore",
    "hong kong": "Hong Kong SAR China",
    "dubai": "United Arab Emirates",
    "abu dhabi": "United Arab Emirates",
    "macau": "Macau SAR China",
    "luxembourg": "Luxembourg",
}

# Sostituzioni sulle etichette di paese (chiave: paese minuscolo).
COUNTRY_ALIASES = {
    "czech republic": "Czechia",
}

# Correzioni puntuali (per Url) di campi invertiti o mancanti.
FIELD_FIXES = {
    "https://guide.michelin.com/en/kyoto-region/kyoto/restaurant/tokumaru": {
        "Cuisine": "Japanese",
        "Price": "\u00a5\u00a5",
    },
}

SCI_NOTATION = re.compile(r"^[+-]?\d+(?:\.\d+)?[eE][+-]?\d+$")


def download_reference(path: str) -> None:
    """Scarica lo snapshot di riferimento, se non gia' presente."""
    if os.path.exists(path):
        return
    print(f"[INFO] Scarico lo snapshot di riferimento in {path} ...", file=sys.stderr)
    with urllib.request.urlopen(REFERENCE_URL, timeout=120) as response:
        with open(path, "wb") as out:
            out.write(response.read())


def load_reference_phones(path: str) -> dict[str, str]:
    """Restituisce una mappa Url -> telefono (cifre) dallo snapshot."""
    phones: dict[str, str] = {}
    if not path or not os.path.exists(path):
        return phones
    with open(path, newline="", encoding="utf-8") as handle:
        for row in csv.DictReader(handle):
            url = row["Url"].strip()
            phone = only_digits(row["PhoneNumber"])
            if url and phone:
                phones[url] = phone
    return phones


def only_digits(value: str) -> str:
    """Tiene solo le cifre di un numero di telefono (rimuove '+' e separatori)."""
    return re.sub(r"\D", "", value or "")


def expand_scientific(value: str) -> str:
    """Espande la notazione scientifica in stringa decimale intera."""
    try:
        number = Decimal(value.strip())
    except Exception:
        return only_digits(value)
    return format(number, "f").split(".")[0]


def normalize_phone(value: str, reference: str | None) -> str:
    """Normalizza un numero di telefono.

    Se e' disponibile il valore corretto dallo snapshot lo usa; altrimenti
    espande la notazione scientifica (con perdita delle cifre troncate).
    """
    if reference:
        return reference
    raw = (value or "").strip()
    if not raw:
        return ""
    if SCI_NOTATION.match(raw):
        return expand_scientific(raw)
    return only_digits(raw)


# Schemi malformati osservati nel dataset (pattern, sostituzione).
BROKEN_SCHEMES = (
    (r"^http://httpss//", "https://"),
    (r"^https://httpss//", "https://"),
    (r"^httpss://", "https://"),
    (r"^httpss//", "https://"),
    (r"^https//", "https://"),
    (r"^http//", "http://"),
)


def normalize_url(value: str) -> str:
    """Normalizza l'URL del sito web rimuovendo gli errori di schema piu' comuni."""
    url = (value or "").strip()
    if not url:
        return ""
    for pattern, replacement in BROKEN_SCHEMES:
        if re.match(pattern, url, flags=re.IGNORECASE):
            url = re.sub(pattern, replacement, url, count=1, flags=re.IGNORECASE)
            break
    if not re.match(r"^https?://", url, flags=re.IGNORECASE):
        url = "https://" + url
    return url


def normalize_location(location: str) -> str:
    """Corregge etichette di paese e assegna il paese alle citta-stato."""
    location = (location or "").strip()
    if not location:
        return location
    if "," in location:
        city, country = (part.strip() for part in location.split(",", 1))
        country = COUNTRY_ALIASES.get(country.lower(), country)
        return f"{city}, {country}"
    country = CITY_COUNTRY.get(location.lower())
    return f"{location}, {country}" if country else location


def clean(original_path: str, clean_path: str, reference_path: str | None) -> tuple[int, int]:
    reference_phones = load_reference_phones(reference_path)
    recovered = 0

    with open(original_path, newline="", encoding="utf-8") as source:
        reader = csv.reader(source)
        header = next(reader)
        rows = list(reader)

    phone_index = header.index("PhoneNumber")
    location_index = header.index("Location")
    website_index = header.index("WebsiteUrl")
    url_index = header.index("Url")
    fixed_fields: dict[str, int] = {name: header.index(name) for name in ("Cuisine", "Price")}

    for row in rows:
        url = row[url_index].strip()
        reference_phone = reference_phones.get(url)

        before = row[phone_index]
        row[phone_index] = normalize_phone(before, reference_phone)
        if reference_phone and before != row[phone_index]:
            recovered += 1

        row[location_index] = normalize_location(row[location_index])
        row[website_index] = normalize_url(row[website_index])

        for name, index in fixed_fields.items():
            if url in FIELD_FIXES and name in FIELD_FIXES[url]:
                row[index] = FIELD_FIXES[url][name]

    with open(clean_path, "w", newline="", encoding="utf-8") as target:
        writer = csv.writer(target, lineterminator="\n")
        writer.writerow(header)
        writer.writerows(rows)

    return len(rows), recovered


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--original", default=ORIGINAL, help="CSV originale di input")
    parser.add_argument("--output", default=CLEAN, help="CSV pulito di output")
    parser.add_argument(
        "--reference",
        default=os.path.join(tempfile.gettempdir(), "michelin_reference.csv"),
        help="Snapshot di riferimento per recuperare i telefoni",
    )
    parser.add_argument(
        "--no-download",
        action="store_true",
        help="Non scaricare lo snapshot; espande solo la notazione scientifica",
    )
    args = parser.parse_args()

    reference_path = args.reference
    if not args.no_download:
        download_reference(reference_path)
    else:
        reference_path = None

    total, recovered = clean(args.original, args.output, reference_path)
    print(
        f"[OK] {total} record scritti in {args.output}; "
        f"{recovered} telefoni recuperati dallo snapshot."
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
