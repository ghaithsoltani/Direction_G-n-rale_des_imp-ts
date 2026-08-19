package tn.gov.dgi.immatriculation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import tn.gov.dgi.immatriculation.dto.response.OcrResultResponseDTO;
import tn.gov.dgi.immatriculation.dto.response.PieceJointeResponseDTO;
import tn.gov.dgi.immatriculation.model.PieceJointe;
import tn.gov.dgi.immatriculation.model.ResultatOcr;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PieceJointeMapper {

    /**
     * "urlTelechargement" est ignoré ici car il dépend du chemin de base de
     * l'API (non disponible dans le mapper) -> renseigné explicitement dans
     * DocumentUploadService après l'appel au mapper (voir étape 5).
     */
    @Mapping(target = "urlTelechargement", ignore = true)
    @Mapping(target = "dossierId", source = "dossier.id")
    PieceJointeResponseDTO toDto(PieceJointe entity);

    List<PieceJointeResponseDTO> toDtoList(List<PieceJointe> entities);

    /**
     * "texteBrutExtrait" volontairement absent de OcrResultResponseDTO
     * -> ignoré automatiquement par unmappedTargetPolicy = IGNORE côté
     * source (le champ source non consommé n'est pas une erreur).
     */
    OcrResultResponseDTO toDto(ResultatOcr entity);
}