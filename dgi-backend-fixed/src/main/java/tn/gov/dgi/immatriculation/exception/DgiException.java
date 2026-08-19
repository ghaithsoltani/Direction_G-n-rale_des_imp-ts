package tn.gov.dgi.immatriculation.exception;

/**
 * Exception de base pour toutes les exceptions métier de l'application.
 * Porte un code d'erreur stable (utilisé dans ErrorResponseDTO.erreur),
 * indépendant du message (qui peut varier/être traduit) — utile côté
 * front pour un traitement programmatique (ex: afficher une icône
 * différente selon le code plutôt que de parser le message).
 */
public abstract class DgiException extends RuntimeException {

    private final String code;

    protected DgiException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}