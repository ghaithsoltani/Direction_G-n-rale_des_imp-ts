package tn.gov.dgi.immatriculation.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.gov.dgi.immatriculation.dto.request.ChatMessageRequestDTO;
import tn.gov.dgi.immatriculation.dto.response.ChatMessageResponseDTO;
import tn.gov.dgi.immatriculation.model.*;
import tn.gov.dgi.immatriculation.repository.ConversationRepository;
import tn.gov.dgi.immatriculation.repository.MessageChatRepository;
import tn.gov.dgi.immatriculation.service.ChatbotService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Chatbot orchestrator — now follows the RAG pipeline:
 *
 *  User message
 *       │
 *       ▼
 *  Intent detection (DossierStatusIntentService)
 *       │ no → Retrieval (FaqMatchingService — top N matches)
 *       │              │
 *       │              ▼
 *       │       Prompt Builder (inject FAQ context + conversation history)
 *       │              │
 *       │              ▼
 *       │           LLM (Groq)  ─── if disabled/error ──→  FAQ direct answer
 *       │
 *       ▼
 *   Answer persisted + returned
 *
 * Priority order:
 *   1. Dossier status intent  → real DB data (never LLM for official status)
 *   2. LLM grounded with FAQ context + conversation history
 *   3. FAQ keyword match direct answer (LLM disabled or failed)
 *   4. Generic fallback
 */
@Service
@RequiredArgsConstructor
public class ChatbotServiceImpl implements ChatbotService {

    /** Number of FAQ entries to retrieve and inject into the LLM context */
    private static final int TOP_K_FAQ = 3;

    private final DossierStatusIntentService dossierStatusIntentService;
    private final LlmClientService llmClientService;
    private final FaqMatchingService faqMatchingService;
    private final ConversationRepository conversationRepository;
    private final MessageChatRepository messageChatRepository;

    @Override
    @Transactional
    public ChatMessageResponseDTO traiterMessage(
            ChatMessageRequestDTO request, UUID userId, Role role) {

        // ---- 1. Resolve or create conversation ----
        Conversation conversation = resolveConversation(
                request.getConversationIdAsUUID(), userId, role);

        // ---- 2. Load conversation history for memory ----
        List<MessageChat> historique = messageChatRepository
                .findByConversationIdOrderByDateCreationAsc(conversation.getId());

        // ---- 3. Persist user message ----
        MessageChat messageUtilisateur = MessageChat.builder()
                .conversation(conversation)
                .expediteur(Expediteur.USER)
                .contenu(request.getMessage())
                .source(SourceReponse.UTILISATEUR)
                .build();
        messageChatRepository.save(messageUtilisateur);

        // ---- 4. Determine response via pipeline ----
        String reponse;
        SourceReponse source;

        if (dossierStatusIntentService.estUneDemandeDeStatut(request.getMessage())) {
            // Path A: real dossier data — never delegate to LLM
            reponse = dossierStatusIntentService.construireReponseStatut(
                    request.getMessage(), userId, role);
            source = SourceReponse.DOSSIER_STATUS;

        } else {
            // Path B: RAG pipeline
            // B1 — Retrieve top-K FAQ entries (keyword ranking)
            List<FaqEntry> faqContext = faqMatchingService
                    .trouverTopK(request.getMessage(), role, TOP_K_FAQ);

            // B2 — Call LLM with FAQ context + conversation history
            String llmReponse = llmClientService.genererReponse(
                    request.getMessage(), role, faqContext, historique);

            if (llmReponse != null) {
                reponse = llmReponse;
                source = SourceReponse.LLM;
            } else {
                // B3 — LLM disabled/failed: use best FAQ match directly
                reponse = faqContext.isEmpty()
                        ? "Je n'ai pas trouvé de réponse à votre question. "
                        + "Veuillez contacter la DGI ou reformuler votre demande."
                        : faqContext.get(0).getReponse();
                source = faqContext.isEmpty() ? SourceReponse.FAQ : SourceReponse.FAQ;
            }
        }

        // ---- 5. Persist bot response ----
        MessageChat messageBot = MessageChat.builder()
                .conversation(conversation)
                .expediteur(Expediteur.ASSISTANT)
                .contenu(reponse)
                .source(source)
                .build();
        messageChatRepository.save(messageBot);

        return ChatMessageResponseDTO.builder()
                .conversationId(conversation.getId())
                .reponse(reponse)
                .source(source)
                .dateReponse(LocalDateTime.now())
                .build();
    }

    private Conversation resolveConversation(UUID conversationId, UUID userId, Role role) {
        if (conversationId != null) {
            return conversationRepository.findById(conversationId)
                    .orElseGet(() -> creerNouvelleConversation(userId, role));
        }
        return creerNouvelleConversation(userId, role);
    }

    private Conversation creerNouvelleConversation(UUID userId, Role role) {
        return conversationRepository.save(Conversation.builder()
                .utilisateurId(userId).role(role).build());
    }
}