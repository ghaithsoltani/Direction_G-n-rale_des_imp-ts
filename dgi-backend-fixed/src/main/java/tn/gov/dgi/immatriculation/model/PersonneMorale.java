package tn.gov.dgi.immatriculation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Table(name = "personnes_morales")
@DiscriminatorValue("PERSONNE_MORALE")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PersonneMorale extends Contribuable {

    @Column(name = "raison_sociale", nullable = false, length = 200)
    private String raisonSociale;

    @Column(name = "registre_commerce", unique = true, length = 30)
    private String registreCommerce;

    @Column(name = "forme_juridique", length = 50)
    private String formeJuridique; // SARL, SA, SUARL, etc.

    @Column(name = "date_creation_entreprise")
    private LocalDate dateCreationEntreprise;

    @Column(name = "capital_social")
    private Double capitalSocial;

    // Représentant légal : @Embeddable, colonnes préfixées fusionnées ici
    @Embedded
    private RepresentantLegal representantLegal;
}