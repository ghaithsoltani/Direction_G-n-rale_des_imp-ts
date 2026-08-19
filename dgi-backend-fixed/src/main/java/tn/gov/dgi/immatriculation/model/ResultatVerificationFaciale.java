package tn.gov.dgi.immatriculation.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResultatVerificationFaciale {

    @Column(name = "face_piece_reference_id")
    private UUID pieceJointeReferenceId;

    @Column(name = "face_piece_photo_live_id")
    private UUID pieceJointePhotoLiveId;

    @Column(name = "face_score_similarite")
    private Double scoreSimilarite;

    @Column(name = "face_seuil_acceptation")
    private Double seuilAcceptation;

    @Column(name = "face_correspondance_validee")
    private Boolean correspondanceValidee;

    @Column(name = "face_date_verification")
    private LocalDateTime dateVerification;

    @Column(name = "face_message_erreur", length = 500)
    private String messageErreur;
}