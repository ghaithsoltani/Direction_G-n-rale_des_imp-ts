package tn.gov.dgi.immatriculation.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Embeddable : ses colonnes sont fusionnées directement dans la table de
 * l'entité propriétaire (ici "contribuables"). Pas de table dédiée, pas
 * d'identité propre — c'est l'équivalent JPA le plus proche du sous-document
 * imbriqué qu'on avait en MongoDB.
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Adresse {

    @Column(name = "adresse_rue", length = 200)
    private String rue;

    @Column(name = "adresse_ville", length = 100)
    private String ville;

    @Column(name = "adresse_code_postal", length = 10)
    private String codePostal;

    @Column(name = "adresse_gouvernorat", length = 100)
    private String gouvernorat;

    @Column(name = "adresse_pays", length = 100)
    private String pays;
}