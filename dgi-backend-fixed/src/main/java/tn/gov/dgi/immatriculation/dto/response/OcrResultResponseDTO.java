package tn.gov.dgi.immatriculation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Ne réexpose volontairement PAS "texteBrutExtrait" (potentiellement long
 * et peu utile côté front) — le mapper l'omettra explicitement.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OcrResultResponseDTO {

    private String nomDetecte;
    private String prenomDetecte;
    private LocalDate dateNaissanceDetectee;
    private String numeroPieceDetecte;
    private Double scoreConfiance;
    private Boolean extractionReussie;
    private String messageErreur;
    private LocalDateTime dateExtraction;
}