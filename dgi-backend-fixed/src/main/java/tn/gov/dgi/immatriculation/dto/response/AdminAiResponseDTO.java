package tn.gov.dgi.immatriculation.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AdminAiResponseDTO {
    private String reponse;
    private String typeReponse;   // ANALYSE, ACTION, RECOMMANDATION, QUESTION
    private ActionEffectuee action;
    private List<String> recommandations;
    private UUID conversationId;
    private LocalDateTime timestamp;

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ActionEffectuee {
        private String type;       // CHANGEMENT_STATUT, ASSIGNATION, BULK_UPDATE
        private int nbDossiers;
        private String details;
        private boolean succes;
    }
}