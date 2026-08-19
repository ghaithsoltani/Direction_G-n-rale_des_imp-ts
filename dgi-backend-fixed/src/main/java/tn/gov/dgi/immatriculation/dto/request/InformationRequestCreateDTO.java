package tn.gov.dgi.immatriculation.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class InformationRequestCreateDTO {
    @NotBlank
    @Size(max = 2000)
    private String message;
}
