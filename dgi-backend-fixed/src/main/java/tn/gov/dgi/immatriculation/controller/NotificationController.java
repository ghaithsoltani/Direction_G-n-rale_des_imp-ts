package tn.gov.dgi.immatriculation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tn.gov.dgi.immatriculation.dto.response.NotificationDTO;
import tn.gov.dgi.immatriculation.security.UtilisateurPrincipal;
import tn.gov.dgi.immatriculation.service.NotificationService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Lister mes notifications")
    public ResponseEntity<List<NotificationDTO>> lister(
            @AuthenticationPrincipal UtilisateurPrincipal principal) {
        return ResponseEntity.ok(notificationService.listerPourUtilisateur(principal.getId()));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Marquer une notification comme lue")
    public ResponseEntity<NotificationDTO> marquerLue(
            @PathVariable UUID id,
            @AuthenticationPrincipal UtilisateurPrincipal principal) {
        return ResponseEntity.ok(notificationService.marquerLue(id, principal.getId()));
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Marquer toutes mes notifications comme lues")
    public ResponseEntity<Map<String, Integer>> marquerToutesLues(
            @AuthenticationPrincipal UtilisateurPrincipal principal) {
        int count = notificationService.marquerToutesLues(principal.getId());
        return ResponseEntity.ok(Map.of("marqueesLues", count));
    }
}
