package tn.gov.dgi.immatriculation.dto.request;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import tn.gov.dgi.immatriculation.model.StatutDossier;
import java.util.List;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor
public class BulkStatusDTO {
    @NotEmpty
    private List<UUID> dossierIds;
    @NotNull
    private StatutDossier statut;
    private String commentaire;
}
