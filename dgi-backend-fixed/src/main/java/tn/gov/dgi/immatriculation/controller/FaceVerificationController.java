package tn.gov.dgi.immatriculation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.gov.dgi.immatriculation.dto.response.FaceVerificationResponseDTO;
import tn.gov.dgi.immatriculation.service.FaceVerificationService;

import java.util.UUID;

@RestController
@RequestMapping("/api/face")
@RequiredArgsConstructor
@Tag(name = "Vérification faciale", description = "Comparaison entre photo live et pièce d'identité")
public class FaceVerificationController {

    private final FaceVerificationService faceVerificationService;

    @PostMapping(value = "/verify", consumes = "multipart/form-data")
    @Operation(summary = "Comparer la photo capturée en live à la pièce de référence du dossier")
    public ResponseEntity<FaceVerificationResponseDTO> verifier(
            @RequestParam UUID dossierId,
            @RequestParam UUID pieceJointeReferenceId,
            @RequestParam("photoLive") MultipartFile photoLive) {
        FaceVerificationResponseDTO resultat = faceVerificationService.verifier(
                dossierId, pieceJointeReferenceId, photoLive);
        return ResponseEntity.ok(resultat);
    }
}