package tn.gov.dgi.immatriculation.exception;

/** Exception générique pour toute ressource introuvable (404). */
public class ResourceNotFoundException extends DgiException {
    public ResourceNotFoundException(String message) {
        super("NOT_FOUND", message);
    }
}
