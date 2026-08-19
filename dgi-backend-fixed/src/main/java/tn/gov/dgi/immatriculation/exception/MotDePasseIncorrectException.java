package tn.gov.dgi.immatriculation.exception;
public class MotDePasseIncorrectException extends DgiException {
    public MotDePasseIncorrectException(String message) {
        super("MOT_DE_PASSE_INCORRECT", message);
    }
}
