package tn.gov.dgi.immatriculation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.gov.dgi.immatriculation.model.StatutDossier;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DossierResponseDTO {

    private UUID id;
    private String numeroDossier;
    private StatutDossier statut;

    /**
     * Résumé léger du contribuable (pas le DTO complet) pour éviter de
     * surcharger la réponse dans les listes paginées ; le détail complet
     * du contribuable se récupère via GET /api/contribuables/{id}.
     */
    private ContribuableResumeDTO contribuable;

    private List<PieceJointeResponseDTO> piecesJointes;
    private List<HistoriqueStatutDTO> historiqueStatuts;
    private FaceVerificationResponseDTO resultatVerificationFaciale;
    private String commentaireAgent;
    private UUID agentTraitantId;
    private LocalDateTime dateCreation;
    private LocalDateTime dateDerniereModification;
    private LocalDateTime dateSoumission;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContribuableResumeDTO {
        private UUID id;
        private String nomAffichage; // nom+prénom OU raison sociale, calculé côté mapper
        private String cin;
        private String email;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HistoriqueStatutDTO {
        private StatutDossier ancienStatut;
        private StatutDossier nouveauStatut;
        private LocalDateTime dateChangement;
        private UUID auteurId;
        private String commentaire;
    }
}