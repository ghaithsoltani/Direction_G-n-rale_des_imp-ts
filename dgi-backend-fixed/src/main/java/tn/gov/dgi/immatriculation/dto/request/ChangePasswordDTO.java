package tn.gov.dgi.immatriculation.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class ChangePasswordDTO {
    @NotBlank
    private String ancienMotDePasse;

    @NotBlank
    @Size(min = 8)
    private String nouveauMotDePasse;
}
