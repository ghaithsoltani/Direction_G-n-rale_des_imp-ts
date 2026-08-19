package tn.gov.dgi.immatriculation.mapper;

import org.mapstruct.*;
import tn.gov.dgi.immatriculation.dto.request.*;
import tn.gov.dgi.immatriculation.dto.response.ContribuableResponseDTO;
import tn.gov.dgi.immatriculation.model.*;

/**
 * componentModel = "spring" : MapStruct génère une implémentation annotée
 * @Component, injectable directement dans les services via @RequiredArgsConstructor.
 *
 * unmappedTargetPolicy = IGNORE : évite les erreurs de compilation sur les
 * champs présents dans l'entité mais absents du DTO (ex: id généré côté
 * base, pas dans le DTO de création).
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ContribuableMapper {

    Adresse toEntity(AdresseDTO dto);
    AdresseDTO toDto(Adresse entity);

    Activite toEntity(ActiviteDTO dto);
    ActiviteDTO toDto(Activite entity);

    RepresentantLegal toEntity(RepresentantLegalDTO dto);
    RepresentantLegalDTO toDto(RepresentantLegal entity);

    /**
     * id, dateCreation, dateDerniereModification ignorés à la création :
     * gérés respectivement par @GeneratedValue et les annotations
     * @CreationTimestamp/@UpdateTimestamp d'Hibernate, jamais fournis par
     * le client.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "dateDerniereModification", ignore = true)
    PersonnePhysique toEntity(ContribuableCreateDTO.PersonnePhysiqueCreateDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "dateDerniereModification", ignore = true)
    PersonneMorale toEntity(ContribuableCreateDTO.PersonneMoraleCreateDTO dto);

    /**
     * Mapping polymorphe manuel : MapStruct ne sait pas générer
     * automatiquement un dispatch basé sur le type runtime d'une classe
     * abstraite -> on le fait explicitement en Java par défaut dans
     * l'interface (méthode "default"), en déléguant aux mappings
     * spécifiques ci-dessus selon instanceof.
     */
    default ContribuableResponseDTO toDto(Contribuable entity) {
        if (entity == null) {
            return null;
        }
        if (entity instanceof PersonnePhysique pp) {
            return toDto(pp);
        }
        if (entity instanceof PersonneMorale pm) {
            return toDto(pm);
        }
        throw new IllegalArgumentException("Type de contribuable non supporté : " + entity.getClass());
    }

    ContribuableResponseDTO toDto(PersonnePhysique entity);

    ContribuableResponseDTO toDto(PersonneMorale entity);
}