package tn.gov.dgi.immatriculation.service;

import org.springframework.web.multipart.MultipartFile;
import tn.gov.dgi.immatriculation.dto.response.FaceVerificationResponseDTO;

import java.util.UUID;

public interface FaceVerificationService {

    /**
     * Compare la photo capturée en live (webcam) à la photo de la pièce
     * d'identité de référence déjà uploadée pour ce dossier, et enregistre
     * le résultat sur le dossier.
     */
    FaceVerificationResponseDTO verifier(UUID dossierId, UUID pieceJointeReferenceId, MultipartFile photoLive);
}