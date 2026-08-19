package tn.gov.dgi.immatriculation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tn.gov.dgi.immatriculation.dto.request.ChatMessageRequestDTO;
import tn.gov.dgi.immatriculation.dto.request.FaqEntryCreateDTO;
import tn.gov.dgi.immatriculation.dto.response.ChatMessageResponseDTO;
import tn.gov.dgi.immatriculation.model.FaqEntry;
import tn.gov.dgi.immatriculation.model.Role;
import tn.gov.dgi.immatriculation.model.RoleCibleFaq;
import tn.gov.dgi.immatriculation.repository.FaqEntryRepository;
import tn.gov.dgi.immatriculation.security.UtilisateurPrincipal;
import tn.gov.dgi.immatriculation.service.ChatbotService;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;
    private final FaqEntryRepository faqEntryRepository;

    @PostMapping("/message")
    public ResponseEntity<ChatMessageResponseDTO> envoyerMessage(
            @Valid @RequestBody ChatMessageRequestDTO requete,
            @AuthenticationPrincipal UtilisateurPrincipal principal) {

        Role role = principal.getAuthorities().stream()
                .filter(a -> a.getAuthority().startsWith("ROLE_"))
                .findFirst()
                .map(a -> {
                    try {
                        return Role.valueOf(a.getAuthority().replace("ROLE_", ""));
                    } catch (IllegalArgumentException e) {
                        return Role.CONTRIBUABLE; // fallback
                    }
                })
                .orElse(Role.CONTRIBUABLE);

        return ResponseEntity.ok(chatbotService.traiterMessage(requete, principal.getId(), role));
    }

    // ----------------- Gestion FAQ (ADMIN uniquement, cf. SecurityConfig) -----------------

    @PostMapping("/faq")
    public ResponseEntity<FaqEntry> creerFaq(@Valid @RequestBody FaqEntryCreateDTO dto) {
        FaqEntry entry = FaqEntry.builder()
                .motsCles(dto.getMotsCles())
                .question(dto.getQuestion())
                .reponse(dto.getReponse())
                .categorie(dto.getCategorie())
                .roleCible(dto.getRoleCible() != null ? dto.getRoleCible() : RoleCibleFaq.TOUS)
                .build();
        return ResponseEntity.ok(faqEntryRepository.save(entry));
    }

    @GetMapping("/faq")
    public ResponseEntity<java.util.List<FaqEntry>> listerFaq() {
        return ResponseEntity.ok(faqEntryRepository.findAll());
    }
}