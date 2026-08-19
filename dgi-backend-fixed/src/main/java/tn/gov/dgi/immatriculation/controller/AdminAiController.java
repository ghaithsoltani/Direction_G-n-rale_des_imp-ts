package tn.gov.dgi.immatriculation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tn.gov.dgi.immatriculation.dto.request.AdminAiChatDTO;
import tn.gov.dgi.immatriculation.dto.response.AdminAiResponseDTO;
import tn.gov.dgi.immatriculation.security.UtilisateurPrincipal;
import tn.gov.dgi.immatriculation.service.impl.AdminAiService;

@RestController
@RequestMapping("/api/admin/ai")
@RequiredArgsConstructor
@Tag(name = "Admin AI", description = "Assistant IA intelligent pour l'administrateur — analyse, recommandations, actions")
public class AdminAiController {

    private final AdminAiService adminAiService;

    @PostMapping("/chat")
    @Operation(summary = "Envoyer un message à l'assistant IA admin",
            description = """
                   L'assistant peut :
                   - Répondre à des questions sur les dossiers et statistiques
                   - Analyser les tendances et donner des recommandations
                   - Exécuter des actions (changement de statut, traitement en lot)
                   
                   Exemples de messages :
                   - "Montre-moi les statistiques actuelles"
                   - "Analyse les tendances de ce mois"
                   - "Combien de dossiers sont en attente ?"
                   - "Passe tous les dossiers soumis en traitement"
                   - "Donne-moi les détails du dossier DGI-2026-000123"
                   """)
    public ResponseEntity<AdminAiResponseDTO> chat(
            @Valid @RequestBody AdminAiChatDTO dto,
            @AuthenticationPrincipal UtilisateurPrincipal principal) {
        return ResponseEntity.ok(adminAiService.traiter(dto.getMessage(), principal.getId()));
    }
}