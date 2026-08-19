package tn.gov.dgi.immatriculation.dto.request;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class UpdateProfileDTO {
    @Size(max = 100) private String prenom;
    @Size(max = 100) private String nom;
    @Size(max = 30)  private String telephone;
    @Size(max = 5)   private String languePreferee;
    private Boolean notifEmail;
    private Boolean notifApp;
}
