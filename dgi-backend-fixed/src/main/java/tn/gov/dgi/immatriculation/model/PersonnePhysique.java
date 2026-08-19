package tn.gov.dgi.immatriculation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

/**
 * Table fille "personnes_physiques" : PK = FK vers contribuables.id
 * (mécanisme standard de la stratégie JOINED).
 */
@Entity
@Table(name = "personnes_physiques")
@DiscriminatorValue("PERSONNE_PHYSIQUE")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PersonnePhysique extends Contribuable {

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "prenom", nullable = false, length = 100)
    private String prenom;

    @Column(name = "date_naissance", nullable = false)
    private LocalDate dateNaissance;

    @Column(name = "lieu_naissance", length = 100)
    private String lieuNaissance;

    @Column(name = "nationalite", length = 100)
    private String nationalite;

    @Column(name = "genre", length = 10)
    private String genre;
}