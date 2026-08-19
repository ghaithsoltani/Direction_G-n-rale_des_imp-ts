package tn.gov.dgi.immatriculation.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResultatOcr {

    @Column(name = "ocr_nom_detecte", length = 100)
    private String nomDetecte;

    @Column(name = "ocr_prenom_detecte", length = 100)
    private String prenomDetecte;

    @Column(name = "ocr_date_naissance_detectee")
    private LocalDate dateNaissanceDetectee;

    @Column(name = "ocr_numero_piece_detecte", length = 30)
    private String numeroPieceDetecte;

    @Column(name = "ocr_score_confiance")
    private Double scoreConfiance;

    @Column(name = "ocr_texte_brut_extrait", columnDefinition = "TEXT")
    private String texteBrutExtrait;

    @Column(name = "ocr_date_extraction")
    private LocalDateTime dateExtraction;

    @Column(name = "ocr_extraction_reussie")
    private Boolean extractionReussie;

    @Column(name = "ocr_message_erreur", length = 500)
    private String messageErreur;
}