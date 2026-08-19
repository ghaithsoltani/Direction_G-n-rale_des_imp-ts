package tn.gov.dgi.immatriculation.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TypePieceJointe {

    // Valeurs envoyées par le frontend Angular
    CIN,
    PASSEPORT,
    REGISTRE_COMMERCE,
    AUTRE,

    // Valeurs legacy (gardées pour compatibilité DB)
    CIN_RECTO,
    CIN_VERSO,
    JUSTIFICATIF_DOMICILE,
    STATUTS_SOCIETE,
    PHOTO_LIVE_WEBCAM;

    /**
     * Conversion case-insensitive depuis String (RequestParam / JSON).
     * Accepte "cin", "CIN", "Cin", "CIN_RECTO", etc.
     */
    @JsonCreator
    public static TypePieceJointe fromString(String value) {
        if (value == null) return AUTRE;
        try {
            return TypePieceJointe.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            // Fallback partiel : "cin" → CIN
            String v = value.trim().toUpperCase();
            if (v.startsWith("CIN"))      return CIN;
            if (v.startsWith("PASSEPORT")) return PASSEPORT;
            if (v.startsWith("REGISTRE")) return REGISTRE_COMMERCE;
            return AUTRE;
        }
    }

    @JsonValue
    public String toJson() {
        return this.name();
    }
}