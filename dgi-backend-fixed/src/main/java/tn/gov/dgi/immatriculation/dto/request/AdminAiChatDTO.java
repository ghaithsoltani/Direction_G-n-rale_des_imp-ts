package tn.gov.dgi.immatriculation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor
public class AdminAiChatDTO {
    @NotBlank @Size(max = 2000)
    private String message;

    /** Optional: conversation thread ID for memory */
    private UUID conversationId;
}