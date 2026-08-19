package tn.gov.dgi.immatriculation.dto.request;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import tn.gov.dgi.immatriculation.model.TypeContribuable;
import tn.gov.dgi.immatriculation.validation.AgeMinimum;
import tn.gov.dgi.immatriculation.validation.CinOuPasseportRequis;

import java.time.LocalDate;

@CinOuPasseportRequis
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ContribuableCreateDTO.PersonnePhysiqueCreateDTO.class, name = "PERSONNE_PHYSIQUE"),
        @JsonSubTypes.Type(value = ContribuableCreateDTO.PersonneMoraleCreateDTO.class, name = "PERSONNE_MORALE")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class ContribuableCreateDTO {

    @NotNull(message = "Le type de contribuable est obligatoire")
    private TypeContribuable type;

    @Pattern(regexp = "^[0-9]{8}$", message = "Le CIN doit contenir 8 chiffres")
    private String cin;

    @Size(max = 30, message = "Le numéro de passeport ne doit pas dépasser 30 caractères")
    private String numeroPasseport;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    @Pattern(regexp = "^[0-9+ ]{8,20}$", message = "Format de téléphone invalide")
    private String telephone;

    @Valid
    private AdresseDTO adresse;

    @Valid
    private ActiviteDTO activite;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class PersonnePhysiqueCreateDTO extends ContribuableCreateDTO {

        // FIX: frontend may send empty string when OCR fails to extract name/prenom
        // Validation is enforced on the frontend; backend accepts null/empty gracefully
        @Size(max = 100)
        private String nom;

        @Size(max = 100)
        private String prenom;

        private LocalDate dateNaissance;

        private String lieuNaissance;
        private String nationalite;
        private String genre;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class PersonneMoraleCreateDTO extends ContribuableCreateDTO {

        @NotBlank(message = "La raison sociale est obligatoire")
        @Size(max = 200)
        private String raisonSociale;

        @NotBlank(message = "Le registre de commerce est obligatoire")
        @Pattern(regexp = "^[A-Z][0-9]{8,9}$", message = "Format de registre de commerce invalide (ex: B123456789)")
        private String registreCommerce;

        private String formeJuridique;

        @PastOrPresent(message = "La date de création de l'entreprise ne peut pas être dans le futur")
        private LocalDate dateCreationEntreprise;

        @PositiveOrZero(message = "Le capital social doit être positif ou nul")
        private Double capitalSocial;

        @Valid
        private RepresentantLegalDTO representantLegal;
    }
}