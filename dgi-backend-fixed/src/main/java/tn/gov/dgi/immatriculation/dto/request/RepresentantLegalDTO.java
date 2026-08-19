package tn.gov.dgi.immatriculation.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RepresentantLegalDTO {

    @Size(max = 100)
    private String nom;

    @Size(max = 100)
    private String prenom;

    @Pattern(regexp = "^[0-9]{8}$", message = "Le CIN doit contenir 8 chiffres")
    private String cin;

    private String qualite;
}