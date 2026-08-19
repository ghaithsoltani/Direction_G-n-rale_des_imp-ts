package tn.gov.dgi.immatriculation.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActiviteDTO {

    // FIX: champ absent du formulaire frontend — rendu optionnel
    @Size(max = 20)
    private String codeActivitePrincipale;

    // FIX: champ "Libellé" optionnel côté backend (validé côté frontend)
    @Size(max = 200)
    private String libelleActivite;

    private String secteurActivite;
    private LocalDate dateDebutActivite;
    private String adresseExercice;
    private Boolean activitePrincipale;
}