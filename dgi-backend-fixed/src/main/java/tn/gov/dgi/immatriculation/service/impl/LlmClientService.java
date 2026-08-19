package tn.gov.dgi.immatriculation.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tn.gov.dgi.immatriculation.model.FaqEntry;
import tn.gov.dgi.immatriculation.model.MessageChat;
import tn.gov.dgi.immatriculation.model.Role;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * LLM client with two RAG improvements:
 *
 * 1. CONTEXT GROUNDING: retrieved FAQ entries are injected into the system
 *    prompt so the LLM answers from real DGI knowledge instead of hallucinating.
 *
 * 2. CONVERSATION MEMORY: the last N messages of the conversation are sent
 *    as part of the messages array so the LLM can reference earlier context.
 */
@Service
public class LlmClientService {

    private static final Logger log = LoggerFactory.getLogger(LlmClientService.class);

    /** Maximum conversation turns to include for memory (1 turn = 1 user + 1 assistant message) */
    private static final int MAX_HISTORY_TURNS = 5;

    @Value("${app.llm.api-key:}")
    private String apiKey;

    @Value("${app.llm.model:llama-3.3-70b-versatile}")
    private String modele;

    @Value("${app.llm.enabled:false}")
    private boolean active;

    @Value("${app.llm.base-url:https://api.groq.com/openai/v1}")
    private String baseUrl;

    private RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public boolean estActif() {
        return active && apiKey != null && !apiKey.isBlank();
    }

    /**
     * Generate a response using:
     * - System prompt grounded with retrieved FAQ context
     * - Recent conversation history for memory
     * - Current user message
     */
    public String genererReponse(
            String messageUtilisateur,
            Role role,
            List<FaqEntry> faqContext,
            List<MessageChat> historiqueConversation) {

        if (!estActif()) return null;

        // ---- 1. Build system prompt with grounded FAQ context ----
        String systemPrompt = construireSystemPrompt(role, faqContext);

        // ---- 2. Build messages array: history + current message ----
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));

        // Add recent conversation history (last N turns)
        List<MessageChat> histoRecent = derniersMessages(historiqueConversation);
        for (MessageChat msg : histoRecent) {
            String llmRole = switch (msg.getExpediteur()) {
                case USER -> "user";
                case ASSISTANT -> "assistant";
            };
            messages.add(Map.of("role", llmRole, "content", msg.getContenu()));
        }

        // Add current user message
        messages.add(Map.of("role", "user", "content", messageUtilisateur));

        // ---- 3. Call LLM ----
        Map<String, Object> body = Map.of(
                "model", modele,
                "max_tokens", 600,
                "temperature", 0.4, // lower = more faithful to context
                "messages", messages
        );

        try {
            String response = restClient.post()
                    .uri("/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .onStatus(status -> status.isError(), (req, res) -> {
                        log.warn("Appel LLM Groq échoué avec statut {}", res.getStatusCode());
                    })
                    .body(String.class);

            if (response == null) return null;

            JsonNode root = objectMapper.readTree(response);
            JsonNode choices = root.path("choices");
            if (choices.isMissingNode() || choices.isEmpty()) return null;
            return choices.get(0).path("message").path("content").asText(null);

        } catch (Exception e) {
            log.error("Échec appel LLM Groq", e);
            return null;
        }
    }

    /** Backward-compatible overload — no context, no history (used in tests etc.) */
    public String genererReponse(String messageUtilisateur, Role role) {
        return genererReponse(messageUtilisateur, role, List.of(), List.of());
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private String construireSystemPrompt(Role role, List<FaqEntry> faqContext) {
        String base = (role == Role.AGENT_DGI || role == Role.ADMIN)
                ? PROMPT_BASE_AGENT : PROMPT_BASE_CONTRIBUABLE;

        if (faqContext.isEmpty()) {
            return base;
        }

        // Inject retrieved FAQ entries as grounding context
        StringBuilder ctx = new StringBuilder(base);
        ctx.append("\n\n--- CONNAISSANCES OFFICIELLES DGI (base ta réponse dessus) ---\n");
        for (int i = 0; i < faqContext.size(); i++) {
            FaqEntry e = faqContext.get(i);
            ctx.append("\n[").append(i + 1).append("] Q: ").append(e.getQuestion())
                    .append("\n    R: ").append(e.getReponse()).append("\n");
        }
        ctx.append("\n--- FIN DES CONNAISSANCES ---\n")
                .append("Si la réponse ne figure pas dans les connaissances ci-dessus, dis-le clairement.")
                .append(" Ne donne JAMAIS de statut ou numéro de dossier précis.");

        return ctx.toString();
    }

    private List<MessageChat> derniersMessages(List<MessageChat> historique) {
        int maxMessages = MAX_HISTORY_TURNS * 2; // each turn = user + assistant
        if (historique.size() <= maxMessages) return historique;
        return historique.subList(historique.size() - maxMessages, historique.size());
    }

    // ---- Base system prompts (role-specific, without context) ----

    private static final String PROMPT_BASE_CONTRIBUABLE = """
            Tu es l'assistant virtuel de la Direction Générale des Impôts (DGI) tunisienne,
            spécialisé dans l'immatriculation fiscale en ligne.
            Réponds uniquement sur ce périmètre : aide au remplissage, pièces à fournir, processus.
            RÈGLE ABSOLUE : ne donne JAMAIS de statut de dossier précis, de numéro, de date ou
            de décision — demande toujours à l'utilisateur de reformuler avec "statut de mon dossier".
            Réponds en français, de façon concise et professionnelle.
            """;

    private static final String PROMPT_BASE_AGENT = """
            Tu es l'assistant interne des agents de la DGI pour le traitement des dossiers.
            Tu expliques les procédures internes, critères de validation/rejet, bonnes pratiques.
            RÈGLE ABSOLUE : ne donne jamais de données factuelles de dossier sans confirmation
            que la source est le système officiel. Invite à consulter le dossier directement.
            Réponds en français, de façon concise et professionnelle.
            """;
}