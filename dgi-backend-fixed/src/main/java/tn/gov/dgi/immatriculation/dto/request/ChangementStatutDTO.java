package tn.gov.dgi.immatriculation.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.gov.dgi.immatriculation.model.StatutDossier;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangementStatutDTO {

    @NotNull(message = "Le nouveau statut est obligatoire")
    private StatutDossier nouveauStatut;

    @Size(max = 1000, message = "Le commentaire ne doit pas dépasser 1000 caractères")
    private String commentaire;
}
