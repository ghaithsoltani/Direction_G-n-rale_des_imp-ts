package tn.gov.dgi.immatriculation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.gov.dgi.immatriculation.model.SourceReponse;

import java.time.LocalDateTime;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ChatMessageResponseDTO {
    private UUID conversationId;
    private String reponse;
    private SourceReponse source;
    private LocalDateTime dateReponse;
}