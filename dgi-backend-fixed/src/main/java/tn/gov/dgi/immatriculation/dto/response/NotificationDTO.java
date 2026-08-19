package tn.gov.dgi.immatriculation.dto.response;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationDTO {
    private UUID id;
    private UUID dossierId;
    private String title;
    private String message;
    private String type;
    private boolean lu;
    private LocalDateTime createdAt;
}
