package tn.gov.dgi.immatriculation.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO minimal pour créer un dossier en BROUILLON, rattaché à un
 * contribuable déjà existant. Les pièces jointes et la soumission finale
 * se font via des endpoints séparés (upload multipart + endpoint
 * de soumission), pas dans ce DTO — cohérent avec le parcours multi-étapes.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DossierCreateDTO {

    @NotNull(message = "L'identifiant du contribuable est obligatoire")
    private UUID contribuableId;
}