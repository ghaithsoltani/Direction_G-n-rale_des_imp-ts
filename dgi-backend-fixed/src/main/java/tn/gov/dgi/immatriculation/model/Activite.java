package tn.gov.dgi.immatriculation.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Activite {

    @Column(name = "activite_code_principale", length = 20)
    private String codeActivitePrincipale;

    @Column(name = "activite_libelle", length = 200)
    private String libelleActivite;

    @Column(name = "activite_secteur", length = 100)
    private String secteurActivite;

    @Column(name = "activite_date_debut")
    private LocalDate dateDebutActivite;

    @Column(name = "activite_adresse_exercice", length = 200)
    private String adresseExercice;

    @Column(name = "activite_principale")
    private Boolean activitePrincipale;
}