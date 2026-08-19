package tn.gov.dgi.immatriculation.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * OCR response returned by POST /ocr/extract
 *
 * All fields are always present in JSON.
 * Missing values are returned as null.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.ALWAYS)
public class CinOcrResponseDTO {

    private String nomDetecte;

    private String prenomDetecte;

    private String numeroPieceDetecte;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateNaissanceDetectee;

    private String lieuNaissance;   // <-- ADD THIS

    private String adresse;

    private String texte;

    private Double confiance;       // <-- ADD THIS
}