package tn.gov.dgi.immatriculation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FaceVerificationResponseDTO {

    private UUID pieceJointeReferenceId;
    private UUID pieceJointePhotoLiveId;
    private Double scoreSimilarite;
    private Double seuilAcceptation;
    private Boolean correspondanceValidee;
    private LocalDateTime dateVerification;
    private String messageErreur;
}