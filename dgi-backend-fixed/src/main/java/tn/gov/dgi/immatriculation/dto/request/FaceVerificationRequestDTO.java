package tn.gov.dgi.immatriculation.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * L'image elle-même (photo webcam) est envoyée en multipart/form-data en
 * parallèle de ce DTO (voir OcrController/FaceVerificationController à
 * l'étape 6) ; ce DTO ne porte que les métadonnées de la requête.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FaceVerificationRequestDTO {

    @NotNull(message = "L'identifiant du dossier est obligatoire")
    private UUID dossierId;

    @NotNull(message = "L'identifiant de la pièce de référence (CIN/passeport) est obligatoire")
    private UUID pieceJointeReferenceId;
}