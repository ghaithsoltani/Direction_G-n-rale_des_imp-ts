package tn.gov.dgi.immatriculation.exception;

/** Levée quand un contribuable référencé par son id/CIN n'existe pas. */
public class ContribuableNotFoundException extends DgiException {
    public ContribuableNotFoundException(String message) {
        super("CONTRIBUABLE_NOT_FOUND", message);
    }
}