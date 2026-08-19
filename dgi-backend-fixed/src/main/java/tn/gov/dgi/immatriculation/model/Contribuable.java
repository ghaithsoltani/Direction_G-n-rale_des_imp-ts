package tn.gov.dgi.immatriculation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Classe de base pour un contribuable.
 *
 * Stratégie d'héritage JOINED (et non SINGLE_TABLE) :
 * - Table "contribuables" : colonnes communes (cin, email, adresse...)
 * - Tables "personnes_physiques" / "personnes_morales" : colonnes spécifiques
 * - Liées par une PK partagée (personnes_physiques.id est aussi FK vers
 *   contribuables.id)
 *
 * Avantage sur SINGLE_TABLE : pas de dizaines de colonnes nullables mélangées
 * dans une seule table (raison_sociale, nom, prenom... tous nullable selon
 * le type), et on peut mettre des contraintes NOT NULL réelles sur les
 * colonnes propres à chaque sous-type au niveau base de données.
 * Coût : une jointure supplémentaire à la lecture (acceptable ici, le volume
 * de contribuables n'est pas de l'ordre du big data).
 */
@Entity
@Table(name = "contribuables")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "type_contribuable", discriminatorType = DiscriminatorType.STRING)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class Contribuable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Colonne discriminante technique gérée par Hibernate via
     * @DiscriminatorColumn. On garde aussi le champ "type" métier explicite
     * ci-dessous par lisibilité et pour ne pas dépendre du nom de la valeur
     * discriminante dans les requêtes JPQL/natives.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, updatable = false)
    private TypeContribuable type;

    @Column(name = "cin", unique = true, length = 20)
    private String cin; // nullable pour étrangers sans CIN tunisienne

    @Column(name = "numero_passeport", length = 30)
    private String numeroPasseport;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "telephone", length = 20)
    private String telephone;

    // --- Adresse : @Embeddable, colonnes fusionnées dans "contribuables" ---
    @Embedded
    private Adresse adresse;

    // --- Activité : @Embeddable, colonnes fusionnées dans "contribuables" ---
    @Embedded
    private Activite activite;

    
    @Column(name = "matricule_fiscale", unique = true, length = 30)
    private String matriculeFiscale;
@CreationTimestamp
    @Column(name = "date_creation", updatable = false, nullable = false)
    private LocalDateTime dateCreation;

    @UpdateTimestamp
    @Column(name = "date_derniere_modification", nullable = false)
    private LocalDateTime dateDerniereModification;
}