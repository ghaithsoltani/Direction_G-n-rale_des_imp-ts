package tn.gov.dgi.immatriculation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.gov.dgi.immatriculation.dto.request.ActiviteDTO;
import tn.gov.dgi.immatriculation.dto.request.AdresseDTO;
import tn.gov.dgi.immatriculation.dto.request.RepresentantLegalDTO;
import tn.gov.dgi.immatriculation.model.TypeContribuable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de réponse unique pour les deux sous-types (à plat), plus simple à
 * consommer côté front qu'un DTO polymorphe en sortie. Les champs propres
 * à un seul sous-type restent simplement null pour l'autre type.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContribuableResponseDTO {

    private UUID id;
    private TypeContribuable type;
    private String cin;
    private String numeroPasseport;
    private String email;
    private String telephone;
    private String matriculeFiscale;
    private AdresseDTO adresse;
    private ActiviteDTO activite;

    // Champs PersonnePhysique (null si PersonneMorale)
    private String nom;
    private String prenom;
    private LocalDate dateNaissance;
    private String lieuNaissance;
    private String nationalite;
    private String genre;

    // Champs PersonneMorale (null si PersonnePhysique)
    private String raisonSociale;
    private String registreCommerce;
    private String formeJuridique;
    private LocalDate dateCreationEntreprise;
    private Double capitalSocial;
    private RepresentantLegalDTO representantLegal;

    private LocalDateTime dateCreation;
    private LocalDateTime dateDerniereModification;
}