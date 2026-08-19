package tn.gov.dgi.immatriculation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.gov.dgi.immatriculation.model.TypePieceJointe;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PieceJointeResponseDTO {

    private UUID id;
    private UUID dossierId;
    private TypePieceJointe typePiece;
    private String nomFichierOriginal;
    private String contentType;
    private Long tailleOctets;
    private OcrResultResponseDTO resultatOcr;
    private LocalDateTime dateUpload;

    /**
     * URL de téléchargement générée par le mapper/service (pas stockée en
     * base), pointant vers l'endpoint de téléchargement du fichier —
     * cf. étape 6, GET /api/dossiers/{dossierId}/documents/{pieceId}.
     */
    private String urlTelechargement;
}