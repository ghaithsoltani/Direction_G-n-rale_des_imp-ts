package tn.gov.dgi.immatriculation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Format d'erreur normalisé, utilisé par le @ControllerAdvice global
 * (voir étape 7).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponseDTO {

    private LocalDateTime timestamp;
    private int status;
    private String erreur; // ex: "DOSSIER_NOT_FOUND"
    private String message;
    private String chemin; // path de la requête
    private List<ErreurChamp> erreursChamps; // détail des erreurs de validation, si applicable

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ErreurChamp {
        private String champ;
        private String message;
    }
}