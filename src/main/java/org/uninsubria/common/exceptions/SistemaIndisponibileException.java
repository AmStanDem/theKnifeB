package org.uninsubria.common.exceptions;

/**
 * Eccezione utilizzata dal Server per incapsulare errori infrastrutturali (es. SQLException)
 * ed evitare che dettagli interni buchino il confine di rete verso il Client.
 */
public class SistemaIndisponibileException extends TheKnifeException {

    public SistemaIndisponibileException() {
        super("Il servizio è temporaneamente non disponibile a causa di un problema tecnico. Riprovare più tardi.", null);
    }

    public SistemaIndisponibileException(Throwable causeLoggataNelServer) {
        // La causa viene passata a null verso il supercostruttore per evitare
        // che il Client tenti di deserializzare eccezioni SQL che non conosce.
        super("Il servizio è temporaneamente non disponibile a causa di un problema tecnico. Riprovare più tardi.", null);
    }
}