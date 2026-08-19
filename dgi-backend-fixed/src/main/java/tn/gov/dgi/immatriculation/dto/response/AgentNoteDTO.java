package tn.gov.dgi.immatriculation.dto.response;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AgentNoteDTO {
    private UUID id;
    private UUID dossierId;
    private UUID agentId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
