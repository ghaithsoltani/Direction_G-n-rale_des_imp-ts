package tn.gov.dgi.immatriculation.exception;

/** Levée quand un dossier référencé par son id n'existe pas. */
public class DossierNotFoundException extends DgiException {
    public DossierNotFoundException(String message) {
        super("DOSSIER_NOT_FOUND", message);
    }
}