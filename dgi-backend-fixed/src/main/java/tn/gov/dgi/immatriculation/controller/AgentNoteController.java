package tn.gov.dgi.immatriculation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tn.gov.dgi.immatriculation.dto.request.AgentNoteCreateDTO;
import tn.gov.dgi.immatriculation.dto.response.AgentNoteDTO;
import tn.gov.dgi.immatriculation.exception.ResourceNotFoundException;
import tn.gov.dgi.immatriculation.model.AgentNote;
import tn.gov.dgi.immatriculation.repository.AgentNoteRepository;
import tn.gov.dgi.immatriculation.security.UtilisateurPrincipal;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dossiers/{dossierId}/notes")
@RequiredArgsConstructor
@Tag(name = "Notes agent", description = "Notes internes — jamais exposées aux contribuables")
public class AgentNoteController {

    private final AgentNoteRepository agentNoteRepository;

    @GetMapping
    @Operation(summary = "Lister les notes internes d'un dossier")
    public ResponseEntity<List<AgentNoteDTO>> lister(@PathVariable UUID dossierId) {
        return ResponseEntity.ok(
                agentNoteRepository.findByDossierIdOrderByCreatedAtDesc(dossierId)
                        .stream().map(this::toDto).collect(Collectors.toList()));
    }

    @PostMapping
    @Operation(summary = "Ajouter une note interne")
    public ResponseEntity<AgentNoteDTO> creer(
            @PathVariable UUID dossierId,
            @Valid @RequestBody AgentNoteCreateDTO dto,
            @AuthenticationPrincipal UtilisateurPrincipal principal) {
        AgentNote note = agentNoteRepository.save(AgentNote.builder()
                .dossierId(dossierId).agentId(principal.getId())
                .content(dto.getContent()).build());
        return ResponseEntity.ok(toDto(note));
    }

    @PutMapping("/{noteId}")
    @Operation(summary = "Modifier une note interne")
    public ResponseEntity<AgentNoteDTO> modifier(
            @PathVariable UUID dossierId,
            @PathVariable UUID noteId,
            @Valid @RequestBody AgentNoteCreateDTO dto,
            @AuthenticationPrincipal UtilisateurPrincipal principal) {
        AgentNote note = agentNoteRepository.findById(noteId)
                .filter(n -> n.getDossierId().equals(dossierId) && n.getAgentId().equals(principal.getId()))
                .orElseThrow(() -> new ResourceNotFoundException( "Note introuvable"));
        note.setContent(dto.getContent());
        return ResponseEntity.ok(toDto(agentNoteRepository.save(note)));
    }

    @DeleteMapping("/{noteId}")
    @Operation(summary = "Supprimer une note interne")
    public ResponseEntity<Void> supprimer(
            @PathVariable UUID dossierId,
            @PathVariable UUID noteId,
            @AuthenticationPrincipal UtilisateurPrincipal principal) {
        AgentNote note = agentNoteRepository.findById(noteId)
                .filter(n -> n.getDossierId().equals(dossierId) && n.getAgentId().equals(principal.getId()))
                .orElseThrow(() -> new ResourceNotFoundException( "Note introuvable"));
        agentNoteRepository.delete(note);
        return ResponseEntity.noContent().build();
    }

    private AgentNoteDTO toDto(AgentNote n) {
        return AgentNoteDTO.builder().id(n.getId()).dossierId(n.getDossierId())
                .agentId(n.getAgentId()).content(n.getContent())
                .createdAt(n.getCreatedAt()).updatedAt(n.getUpdatedAt()).build();
    }
}
