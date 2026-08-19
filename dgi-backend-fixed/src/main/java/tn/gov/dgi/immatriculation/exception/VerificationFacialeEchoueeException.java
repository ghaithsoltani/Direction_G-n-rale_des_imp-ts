package tn.gov.dgi.immatriculation.exception;

/** Levée quand la comparaison faciale échoue techniquement (visage non détecté, image illisible). */
public class VerificationFacialeEchoueeException extends DgiException {
    public VerificationFacialeEchoueeException(String message) {
        super("VERIFICATION_FACIALE_ECHOUEE", message);
    }
}