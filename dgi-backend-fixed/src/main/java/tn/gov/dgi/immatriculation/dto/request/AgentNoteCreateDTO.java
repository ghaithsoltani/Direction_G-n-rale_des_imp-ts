package tn.gov.dgi.immatriculation.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class AgentNoteCreateDTO {
    @NotBlank
    @Size(max = 5000)
    private String content;
}
