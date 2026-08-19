package tn.gov.dgi.immatriculation.exception;

/**
 * Levée quand une transition de statut de dossier n'est pas autorisée
 * (ex: passer directement de BROUILLON à VALIDE). Mappée en 409 Conflict
 * plutôt qu'en 400 Bad Request : la requête est syntaxiquement valide,
 * c'est l'état actuel de la ressource qui rend l'opération impossible —
 * distinction sémantique importante en REST.
 */
public class TransitionStatutInvalideException extends DgiException {
    public TransitionStatutInvalideException(String message) {
        super("TRANSITION_STATUT_INVALIDE", message);
    }
}