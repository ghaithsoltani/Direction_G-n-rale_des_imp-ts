package tn.gov.dgi.immatriculation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.gov.dgi.immatriculation.dto.request.ChangementStatutDTO;
import tn.gov.dgi.immatriculation.dto.request.BulkStatusDTO;
import tn.gov.dgi.immatriculation.dto.request.DossierCreateDTO;
import tn.gov.dgi.immatriculation.dto.response.DossierResponseDTO;
import tn.gov.dgi.immatriculation.dto.response.PieceJointeResponseDTO;
import tn.gov.dgi.immatriculation.dto.response.StatistiquesDashboardDTO;
import tn.gov.dgi.immatriculation.model.StatutDossier;
import tn.gov.dgi.immatriculation.model.PieceJointe;
import tn.gov.dgi.immatriculation.model.TypePieceJointe;
import tn.gov.dgi.immatriculation.security.UtilisateurPrincipal;
import tn.gov.dgi.immatriculation.service.DocumentUploadService;
import tn.gov.dgi.immatriculation.service.DossierService;
import tn.gov.dgi.immatriculation.repository.DossierImmatriculationRepository;
import tn.gov.dgi.immatriculation.repository.PieceJointeRepository;
import tn.gov.dgi.immatriculation.mapper.PieceJointeMapper;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/dossiers")
@RequiredArgsConstructor
@Tag(name = "Dossiers", description = "Gestion des dossiers d'immatriculation fiscale")
public class DossierController {

    private final DossierService dossierService;
    private final DocumentUploadService documentUploadService;
    private final PieceJointeRepository pieceJointeRepository;
    private final PieceJointeMapper pieceJointeMapper;
    private final DossierImmatriculationRepository dossierRepository;

    @PostMapping
    @Operation(summary = "Créer un dossier en BROUILLON")
    public ResponseEntity<DossierResponseDTO> creer(@Valid @RequestBody DossierCreateDTO dto) {
        DossierResponseDTO cree = dossierService.creerBrouillon(dto);
        return ResponseEntity
                .created(URI.create("/api/dossiers/" + cree.getId()))
                .body(cree);
    }

    /**
     * FIX: /statistiques MUST be declared before /{id}.
     * Spring MVC resolves literal path segments before path variables,
     * but only when both are at the same mapping level. Declaring this
     * method first ensures it is registered first in the handler mapping.
     */
    @GetMapping("/statistiques")
    @Operation(summary = "Statistiques globales pour le dashboard agent (compteurs + évolution mensuelle)")
    public ResponseEntity<StatistiquesDashboardDTO> obtenirStatistiques() {
        return ResponseEntity.ok(dossierService.obtenirStatistiques());
    }

    @GetMapping
    @Operation(summary = "Liste paginée des dossiers avec filtres (usage agent DGI)")
    public ResponseEntity<Page<DossierResponseDTO>> lister(
            @RequestParam(required = false) StatutDossier statut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFin,
            Pageable pageable) {
        return ResponseEntity.ok(dossierService.rechercherAvecFiltres(statut, dateDebut, dateFin, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir un dossier par son identifiant")
    public ResponseEntity<DossierResponseDTO> obtenirParId(@PathVariable UUID id) {
        return ResponseEntity.ok(dossierService.obtenirParId(id));
    }

    @GetMapping("/contribuable/{contribuableId}")
    @Operation(summary = "Lister tous les dossiers d'un contribuable")
    public ResponseEntity<List<DossierResponseDTO>> listerParContribuable(@PathVariable UUID contribuableId) {
        return ResponseEntity.ok(dossierService.listerParContribuable(contribuableId));
    }

    @PostMapping("/{id}/soumettre")
    @Operation(summary = "Soumettre un dossier BROUILLON (passage à SOUMIS)")
    public ResponseEntity<DossierResponseDTO> soumettre(
            @PathVariable UUID id,
            @AuthenticationPrincipal UtilisateurPrincipal principal) {
        return ResponseEntity.ok(dossierService.soumettre(id, principal.getId()));
    }

    @PutMapping("/{id}/statut")
    @Operation(summary = "Changer le statut d'un dossier (validation/rejet par l'agent DGI)")
    public ResponseEntity<DossierResponseDTO> changerStatut(
            @PathVariable UUID id,
            @Valid @RequestBody ChangementStatutDTO dto,
            @AuthenticationPrincipal UtilisateurPrincipal principal) {
        return ResponseEntity.ok(dossierService.changerStatut(id, dto, principal.getId()));
    }

    @PostMapping(value = "/{dossierId}/documents", consumes = "multipart/form-data")
    @Operation(summary = "Uploader une pièce jointe pour un dossier")
    public ResponseEntity<PieceJointeResponseDTO> uploaderDocument(
            @PathVariable UUID dossierId,
            @RequestParam TypePieceJointe typePiece,
            @RequestParam("fichier") MultipartFile fichier) {
        PieceJointeResponseDTO piece = documentUploadService.uploaderDocument(dossierId, typePiece, fichier);
        return ResponseEntity
                .created(URI.create("/api/dossiers/" + dossierId + "/documents/" + piece.getId()))
                .body(piece);
    }

    @GetMapping("/{dossierId}/documents")
    @Operation(summary = "Lister toutes les pièces jointes d'un dossier (agent/admin)")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<List<PieceJointeResponseDTO>> listerDocuments(
            @PathVariable UUID dossierId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        // Verify dossier exists — returns 404 if not
        if (!dossierRepository.existsById(dossierId)) {
            throw new tn.gov.dgi.immatriculation.exception.DossierNotFoundException(
                    "Aucun dossier trouvé avec l'id " + dossierId);
        }
        // Extract raw token for embedding in preview URLs
        String token = (authHeader != null && authHeader.startsWith("Bearer "))
                ? authHeader.substring(7) : "";

        List<PieceJointeResponseDTO> docs = pieceJointeRepository
                .findByDossierIdOrderByDateUploadAsc(dossierId)
                .stream()
                .map((PieceJointe p) -> {
                    PieceJointeResponseDTO dto = pieceJointeMapper.toDto(p);
                    // Include token in URL so <img src="..."> works without extra headers
                    dto.setUrlTelechargement(
                            "/api/dossiers/" + dossierId + "/documents/" + p.getId()
                                    + (token.isEmpty() ? "" : "?token=" + token));
                    return dto;
                })
                .toList();
        return ResponseEntity.ok(docs);
    }

    @GetMapping("/{dossierId}/documents/{pieceId}")
    @Operation(summary = "Télécharger / visualiser une pièce jointe")
    public ResponseEntity<Resource> telechargerDocument(
            @PathVariable UUID dossierId,
            @PathVariable UUID pieceId) {
        // Ownership check: pieceId must belong to dossierId
        PieceJointeResponseDTO meta = documentUploadService.obtenirMetadonnees(pieceId);
        if (!dossierId.equals(meta.getDossierId())) {
            throw new tn.gov.dgi.immatriculation.exception.DossierNotFoundException(
                    "Document " + pieceId + " introuvable pour le dossier " + dossierId);
        }
        Resource fichier = documentUploadService.telechargerDocument(pieceId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        meta.getContentType() != null ? meta.getContentType() : "application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + meta.getNomFichierOriginal() + "\"")
                .body(fichier);
    }

    @PostMapping("/bulk-status")
    @Operation(summary = "Mettre à jour le statut de plusieurs dossiers (agent/admin)")
    public ResponseEntity<java.util.Map<String, Object>> bulkChangerStatut(
            @Valid @RequestBody BulkStatusDTO dto,
            @AuthenticationPrincipal UtilisateurPrincipal principal) {
        java.util.List<String> erreurs = new java.util.ArrayList<>();
        int succes = 0;
        for (java.util.UUID dossierId : dto.getDossierIds()) {
            try {
                dossierService.changerStatut(dossierId,
                        new ChangementStatutDTO(dto.getStatut(), dto.getCommentaire()),
                        principal.getId());
                succes++;
            } catch (Exception e) {
                erreurs.add(dossierId + ": " + e.getMessage());
            }
        }
        return ResponseEntity.ok(java.util.Map.of("succes", succes, "erreurs", erreurs));
    }

    @DeleteMapping("/{dossierId}/documents/{pieceId}")
    @Operation(summary = "Supprimer une pièce jointe")
    public ResponseEntity<Void> supprimerDocument(
            @PathVariable UUID dossierId,
            @PathVariable UUID pieceId) {
        documentUploadService.supprimerDocument(pieceId);
        return ResponseEntity.noContent().build();
    }
}