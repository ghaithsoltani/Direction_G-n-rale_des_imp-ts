package tn.gov.dgi.immatriculation.dto.response;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class InformationRequestDTO {
    private UUID id;
    private UUID dossierId;
    private UUID requestedBy;
    private String message;
    private String statut;
    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;
}
