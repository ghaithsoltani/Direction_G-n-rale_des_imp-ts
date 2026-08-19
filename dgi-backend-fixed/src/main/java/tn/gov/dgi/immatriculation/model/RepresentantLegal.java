package tn.gov.dgi.immatriculation.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RepresentantLegal {

    @Column(name = "representant_nom", length = 100)
    private String nom;

    @Column(name = "representant_prenom", length = 100)
    private String prenom;

    @Column(name = "representant_cin", length = 20)
    private String cin;

    @Column(name = "representant_qualite", length = 100)
    private String qualite; // Gérant, PDG, etc.
}