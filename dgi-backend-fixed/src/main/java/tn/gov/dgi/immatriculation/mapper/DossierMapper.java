package tn.gov.dgi.immatriculation.mapper;

import org.mapstruct.*;
import tn.gov.dgi.immatriculation.dto.response.DossierResponseDTO;
import tn.gov.dgi.immatriculation.model.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = {PieceJointeMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DossierMapper {

    @Mapping(target = "contribuable", source = "contribuable", qualifiedByName = "toResume")
    DossierResponseDTO toDto(DossierImmatriculation entity);

    List<DossierResponseDTO> toDtoList(List<DossierImmatriculation> entities);

    @Named("toResume")
    default DossierResponseDTO.ContribuableResumeDTO toResume(Contribuable contribuable) {
        if (contribuable == null) return null;

        String nomAffichage;

        // Switch pattern matching échoue sur les proxies CGLIB d'Hibernate
        // (JOINED inheritance wraps entities in subclass proxies).
        // instanceof est résolu correctement même sur un proxy.
        if (contribuable instanceof PersonnePhysique pp) {
            String prenom = pp.getPrenom() != null ? pp.getPrenom() : "";
            String nom    = pp.getNom()    != null ? pp.getNom()    : "";
            String full   = (prenom + " " + nom).trim();
            nomAffichage  = full.isEmpty() ? pp.getEmail() : full;
        } else if (contribuable instanceof PersonneMorale pm) {
            nomAffichage = pm.getRaisonSociale();
        } else {
            nomAffichage = contribuable.getEmail();
        }

        return new DossierResponseDTO.ContribuableResumeDTO(
                contribuable.getId(),
                nomAffichage,
                contribuable.getCin(),
                contribuable.getEmail()
        );
    }

    DossierResponseDTO.HistoriqueStatutDTO toDto(HistoriqueStatut entity);

    tn.gov.dgi.immatriculation.dto.response.FaceVerificationResponseDTO toDto(ResultatVerificationFaciale entity);

    ResultatVerificationFaciale toEntity(tn.gov.dgi.immatriculation.dto.response.FaceVerificationResponseDTO dto);
}