package tn.gov.dgi.immatriculation.exception;

/** Levée quand l'extraction OCR échoue techniquement (image illisible, Tesseract en erreur). */
public class OcrExtractionException extends DgiException {
    public OcrExtractionException(String message) {
        super("OCR_EXTRACTION_ECHOUEE", message);
    }
}