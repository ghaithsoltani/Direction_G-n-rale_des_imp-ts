package tn.gov.dgi.immatriculation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tn.gov.dgi.immatriculation.dto.request.ChangementStatutDTO;
import tn.gov.dgi.immatriculation.dto.request.InformationRequestCreateDTO;
import tn.gov.dgi.immatriculation.dto.response.InformationRequestDTO;
import tn.gov.dgi.immatriculation.exception.DossierNotFoundException;
import tn.gov.dgi.immatriculation.model.*;
import tn.gov.dgi.immatriculation.repository.DossierImmatriculationRepository;
import tn.gov.dgi.immatriculation.repository.InformationRequestRepository;
import tn.gov.dgi.immatriculation.security.UtilisateurPrincipal;
import tn.gov.dgi.immatriculation.service.DossierService;
import tn.gov.dgi.immatriculation.service.NotificationService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dossiers/{dossierId}/information-requests")
@RequiredArgsConstructor
@Tag(name = "Demandes d'information")
public class InformationRequestController {

    private final InformationRequestRepository infoRequestRepository;
    private final DossierImmatriculationRepository dossierRepository;
    private final DossierService dossierService;
    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Lister les demandes d'information d'un dossier")
    public ResponseEntity<List<InformationRequestDTO>> lister(@PathVariable UUID dossierId) {
        return ResponseEntity.ok(
                infoRequestRepository.findByDossierIdOrderByCreatedAtDesc(dossierId)
                        .stream().map(this::toDto).collect(Collectors.toList()));
    }

    @PostMapping
    @Operation(summary = "Créer une demande d'information (agent → contribuable)")
    public ResponseEntity<InformationRequestDTO> creer(
            @PathVariable UUID dossierId,
            @Valid @RequestBody InformationRequestCreateDTO dto,
            @AuthenticationPrincipal UtilisateurPrincipal principal) {

        DossierImmatriculation dossier = dossierRepository.findById(dossierId)
                .orElseThrow(() -> new DossierNotFoundException("Dossier introuvable"));

        // Transition EN_TRAITEMENT → EN_ATTENTE_CONTRIBUABLE
        dossierService.changerStatut(dossierId,
                new ChangementStatutDTO(StatutDossier.EN_ATTENTE_CONTRIBUABLE, dto.getMessage()),
                principal.getId());

        InformationRequest request = infoRequestRepository.save(InformationRequest.builder()
                .dossierId(dossierId)
                .requestedBy(principal.getId())
                .message(dto.getMessage())
                .build());

        // Notify the contribuable (find user by contribuableId)
        UUID contribuableUserId = dossier.getContribuable().getId();
        notificationService.creer(
                contribuableUserId, dossierId,
                "Informations complémentaires requises",
                "Votre dossier " + dossier.getNumeroDossier() + " nécessite des informations supplémentaires : " + dto.getMessage(),
                "DEMANDE_INFO");

        return ResponseEntity.ok(toDto(request));
    }

    @PostMapping("/{requestId}/respond")
    @Operation(summary = "Répondre à une demande d'information (contribuable)")
    public ResponseEntity<InformationRequestDTO> repondre(
            @PathVariable UUID dossierId,
            @PathVariable UUID requestId,
            @AuthenticationPrincipal UtilisateurPrincipal principal) {

        InformationRequest req = infoRequestRepository.findById(requestId)
                .orElseThrow(() -> new tn.gov.dgi.immatriculation.exception.ResourceNotFoundException( "Demande introuvable"));

        req.setStatut("REPONDU");
        req.setRespondedAt(LocalDateTime.now());
        infoRequestRepository.save(req);

        // Return dossier to EN_TRAITEMENT
        dossierService.changerStatut(dossierId,
                new ChangementStatutDTO(StatutDossier.EN_TRAITEMENT, "Réponse du contribuable reçue"),
                principal.getId());

        return ResponseEntity.ok(toDto(req));
    }

    private InformationRequestDTO toDto(InformationRequest r) {
        return InformationRequestDTO.builder()
                .id(r.getId()).dossierId(r.getDossierId())
                .requestedBy(r.getRequestedBy()).message(r.getMessage())
                .statut(r.getStatut()).createdAt(r.getCreatedAt()).respondedAt(r.getRespondedAt())
                .build();
    }
}
