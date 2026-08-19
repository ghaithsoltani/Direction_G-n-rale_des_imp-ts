package tn.gov.dgi.immatriculation.exception;

/**
 * Levée pour toute pièce jointe/document invalide : format non autorisé,
 * taille excessive, fichier corrompu, unicité violée (CIN/registre de
 * commerce déjà utilisé), fichier physique introuvable sur le disque.
 */
public class DocumentInvalideException extends DgiException {
    public DocumentInvalideException(String message) {
        super("DOCUMENT_INVALIDE", message);
    }
}