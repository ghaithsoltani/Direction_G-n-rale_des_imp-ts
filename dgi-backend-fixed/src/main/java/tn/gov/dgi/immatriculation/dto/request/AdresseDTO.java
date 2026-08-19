package tn.gov.dgi.immatriculation.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdresseDTO {

    @Size(max = 200)
    private String rue;

    @Size(max = 100)
    private String ville;

    @Size(max = 10)
    private String codePostal;

    @Size(max = 100)
    private String gouvernorat;

    @Size(max = 100)
    private String pays;
}