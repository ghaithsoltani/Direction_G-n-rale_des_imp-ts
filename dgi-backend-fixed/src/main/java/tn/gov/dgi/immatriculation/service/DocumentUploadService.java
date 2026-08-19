package tn.gov.dgi.immatriculation.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import tn.gov.dgi.immatriculation.dto.response.PieceJointeResponseDTO;
import tn.gov.dgi.immatriculation.model.TypePieceJointe;

import java.util.UUID;

public interface DocumentUploadService {

    PieceJointeResponseDTO uploaderDocument(UUID dossierId, TypePieceJointe typePiece, MultipartFile fichier);

    Resource telechargerDocument(UUID pieceJointeId);

    PieceJointeResponseDTO obtenirMetadonnees(UUID pieceJointeId);

    void supprimerDocument(UUID pieceJointeId);
}