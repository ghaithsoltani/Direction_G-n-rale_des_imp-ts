package tn.gov.dgi.immatriculation.dto.response;
import lombok.*;
import tn.gov.dgi.immatriculation.model.Role;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class UserProfileDTO {
    private UUID id;
    private String email;
    private String prenom;
    private String nom;
    private String telephone;
    private Role role;
    private String languePreferee;
    private boolean notifEmail;
    private boolean notifApp;
    private UUID contribuableId;
}
