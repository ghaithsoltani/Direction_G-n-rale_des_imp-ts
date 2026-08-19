package tn.gov.dgi.immatriculation.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import tn.gov.dgi.immatriculation.dto.request.ChangementStatutDTO;
import tn.gov.dgi.immatriculation.dto.response.AdminAiResponseDTO;
import tn.gov.dgi.immatriculation.dto.response.StatistiquesDashboardDTO;
import tn.gov.dgi.immatriculation.model.*;
import tn.gov.dgi.immatriculation.repository.DossierImmatriculationRepository;
import tn.gov.dgi.immatriculation.service.DossierService;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Admin AI — Intent-first routing.
 *
 * Architecture:
 *   User message
 *       │
 *       ▼
 *   detecterIntention()  ← keyword/pattern matching (fast, reliable)
 *       │
 *       ├── STATS      → fetch real stats from DB
 *       ├── DOSSIER    → fetch dossier from DB
 *       ├── ACTION     → execute action on DB
 *       ├── TENDANCES  → fetch stats + build trend report
 *       └── GENERAL    → no data fetch
 *       │
 *       ▼
 *   narerAvecLlm()  ← LLM receives real data + message, writes the answer
 *       │
 *       ▼
 *   AdminAiResponseDTO
 *
 * The LLM NEVER decides what data to fetch — it only narrates.
 * This makes it reliable even with models that don't support tool-calling.
 */
@Service
@RequiredArgsConstructor
public class AdminAiService {

    private static final Logger log = LoggerFactory.getLogger(AdminAiService.class);

    private final DossierService dossierService;
    private final DossierImmatriculationRepository dossierRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.llm.api-key:}")
    private String apiKey;
    @Value("${app.llm.model:llama-3.3-70b-versatile}")
    private String modele;
    @Value("${app.llm.base-url:https://api.groq.com/openai/v1}")
    private String baseUrl;

    private RestClient restClient;

    @PostConstruct
    public void init() {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    // -------------------------------------------------------------------------
    // Main entry point
    // -------------------------------------------------------------------------

    @Transactional
    public AdminAiResponseDTO traiter(String message, UUID adminId) {
        Intention intention = detecterIntention(message);
        log.info("Admin AI intention détectée: {} pour message: '{}'", intention, message);

        return switch (intention) {
            case STATS      -> traiterStats(message);
            case TENDANCES  -> traiterTendances(message);
            case DOSSIER    -> traiterDossier(message);
            case ACTION_STATUT -> traiterActionStatut(message, adminId);
            case BULK_ACTION   -> traiterBulkAction(message, adminId);
            case GENERAL    -> traiterGeneral(message);
        };
    }

    // -------------------------------------------------------------------------
    // Intent detection — keyword matching, no LLM needed
    // -------------------------------------------------------------------------

    private enum Intention {
        STATS, TENDANCES, DOSSIER, ACTION_STATUT, BULK_ACTION, GENERAL
    }

    private Intention detecterIntention(String msg) {
        String m = msg.toLowerCase();

        // Bulk action keywords
        if ((contient(m, "tous", "toutes", "lot", "ensemble", "batch") &&
                contient(m, "passe", "traite", "valide", "rejette", "assigne")))
            return Intention.BULK_ACTION;

        // Single action keywords
        if (contient(m, "passe", "change", "valide", "rejette", "assigne", "modifier le statut") &&
                (m.contains("dgi-") || contient(m, "dossier", "ce dossier")))
            return Intention.ACTION_STATUT;

        // Dossier lookup
        if (m.contains("dgi-") || (contient(m, "dossier", "détail", "info") &&
                contient(m, "donne", "montre", "affiche", "qui", "quel", "comment")))
            return Intention.DOSSIER;

        // Trends
        if (contient(m, "tendance", "trend", "évolution", "mois", "mensuel", "année",
                "pic", "recommandation", "analyse"))
            return Intention.TENDANCES;

        // Stats
        if (contient(m, "statistique", "stat", "tableau de bord", "dashboard", "combien",
                "nombre", "total", "count", "bilan", "état", "actuel", "situation",
                "montre", "affiche", "résumé"))
            return Intention.STATS;

        return Intention.GENERAL;
    }

    private boolean contient(String texte, String... mots) {
        for (String mot : mots) if (texte.contains(mot)) return true;
        return false;
    }

    // -------------------------------------------------------------------------
    // Handlers — each fetches real data then narrates with LLM
    // -------------------------------------------------------------------------

    private AdminAiResponseDTO traiterStats(String message) {
        StatistiquesDashboardDTO stats = dossierService.obtenirStatistiques();

        String donnees = String.format("""
                DONNÉES RÉELLES DU SYSTÈME (extraites à l'instant) :
                - Total dossiers : %d
                - Brouillons : %d
                - Soumis (en attente d'assignation) : %d
                - En traitement : %d
                - En attente contribuable : %d
                - Validés : %d
                - Rejetés : %d
                - Taux de validation : %.1f%%
                - Taux de rejet : %.1f%%
                """,
                stats.getTotalDossiers(), stats.getBrouillons(), stats.getSoumis(),
                stats.getEnTraitement(),
                stats.getTotalDossiers() - stats.getBrouillons() - stats.getSoumis()
                        - stats.getEnTraitement() - stats.getValides() - stats.getRejetes(),
                stats.getValides(), stats.getRejetes(),
                stats.getTotalDossiers() > 0 ? stats.getValides() * 100.0 / stats.getTotalDossiers() : 0,
                stats.getTotalDossiers() > 0 ? stats.getRejetes() * 100.0 / stats.getTotalDossiers() : 0
        );

        String reponse = narerAvecLlm(message, donnees,
                "Présente ces statistiques de façon claire avec des emojis. " +
                        "Mets en évidence les points d'attention (ex: beaucoup de dossiers en attente). " +
                        "Donne 1-2 recommandations concrètes basées sur ces chiffres.");

        List<String> recs = new ArrayList<>();
        if (stats.getSoumis() > 5)
            recs.add(stats.getSoumis() + " dossiers soumis non assignés — à assigner en priorité.");
        if (stats.getEnTraitement() > 15)
            recs.add("Volume élevé en traitement (" + stats.getEnTraitement() + ") — envisager un traitement en lot.");

        return AdminAiResponseDTO.builder()
                .reponse(reponse)
                .typeReponse("ANALYSE")
                .recommandations(recs.isEmpty() ? null : recs)
                .timestamp(LocalDateTime.now())
                .build();
    }

    private AdminAiResponseDTO traiterTendances(String message) {
        StatistiquesDashboardDTO stats = dossierService.obtenirStatistiques();
        List<Long> evol = stats.getEvolutionMensuelle();
        String[] moisNoms = {"Janvier","Février","Mars","Avril","Mai","Juin",
                "Juillet","Août","Septembre","Octobre","Novembre","Décembre"};

        StringBuilder evolStr = new StringBuilder("Évolution mensuelle des dossiers cette année :\n");
        long max = evol.stream().mapToLong(Long::longValue).max().orElse(1);
        for (int i = 0; i < 12; i++) {
            long v = evol.get(i);
            String barre = "█".repeat((int)(v > 0 ? Math.max(1, v * 8 / Math.max(max, 1)) : 0));
            evolStr.append(String.format("%-10s : %3d %s\n", moisNoms[i], v, barre));
        }

        long moisPicVal = max;
        int moisPicIdx = evol.indexOf(max);
        evolStr.append(String.format("\nPic : %s (%d dossiers)", moisNoms[moisPicIdx], moisPicVal));

        String reponse = narerAvecLlm(message, evolStr.toString(),
                "Analyse ces tendances. Identifie les patterns saisonniers, les pics et creux. " +
                        "Donne des recommandations actionnables pour l'administrateur.");

        List<String> recs = new ArrayList<>();
        if (moisPicVal > 0)
            recs.add("Renforcer les effectifs autour de " + moisNoms[moisPicIdx] + " (mois de pic).");
        long dernierMois = evol.get(LocalDateTime.now().getMonthValue() - 1);
        if (dernierMois > max * 0.8)
            recs.add("Le mois actuel est proche du pic — surveiller la charge de travail.");

        return AdminAiResponseDTO.builder()
                .reponse(reponse)
                .typeReponse("RECOMMANDATION")
                .recommandations(recs)
                .timestamp(LocalDateTime.now())
                .build();
    }

    private AdminAiResponseDTO traiterDossier(String message) {
        // Extract dossier number from message (pattern DGI-YYYY-NNNNNN)
        String numero = extraireNumeroDossier(message);
        if (numero == null) {
            return AdminAiResponseDTO.builder()
                    .reponse("Quel est le numéro du dossier ? (format : DGI-2026-000001)")
                    .typeReponse("QUESTION")
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        var opt = dossierRepository.findByNumeroDossier(numero);
        if (opt.isEmpty()) {
            return AdminAiResponseDTO.builder()
                    .reponse("❌ Dossier **" + numero + "** introuvable dans le système.")
                    .typeReponse("ERREUR")
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        DossierImmatriculation d = opt.get();
        String donnees = String.format("""
                DOSSIER %s :
                - Statut actuel : %s
                - Contribuable : %s (email: %s)
                - Date de création : %s
                - Date de soumission : %s
                - Agent traitant : %s
                - Pièces jointes : %d fichier(s)
                - Commentaire agent : %s
                """,
                d.getNumeroDossier(), d.getStatut(),
                d.getContribuable() != null ? d.getContribuable().getEmail() : "N/A",
                d.getContribuable() != null ? d.getContribuable().getEmail() : "N/A",
                d.getDateCreation(), d.getDateSoumission(),
                d.getAgentTraitantId() != null ? d.getAgentTraitantId() : "Non assigné",
                d.getPiecesJointes() != null ? d.getPiecesJointes().size() : 0,
                d.getCommentaireAgent() != null ? d.getCommentaireAgent() : "Aucun"
        );

        String reponse = narerAvecLlm(message, donnees,
                "Présente ces informations clairement. Si le statut suggère une action " +
                        "(ex: soumis mais pas assigné), mentionne-le.");

        return AdminAiResponseDTO.builder()
                .reponse(reponse)
                .typeReponse("ANALYSE")
                .timestamp(LocalDateTime.now())
                .build();
    }

    private AdminAiResponseDTO traiterActionStatut(String message, UUID adminId) {
        String numero = extraireNumeroDossier(message);
        if (numero == null) {
            return AdminAiResponseDTO.builder()
                    .reponse("Quel est le numéro du dossier à modifier ?")
                    .typeReponse("QUESTION")
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        StatutDossier nouveauStatut = extraireStatut(message);
        if (nouveauStatut == null) {
            return AdminAiResponseDTO.builder()
                    .reponse("Quel statut souhaitez-vous appliquer ? (EN_TRAITEMENT, VALIDE, REJETE...)")
                    .typeReponse("QUESTION")
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        var opt = dossierRepository.findByNumeroDossier(numero);
        if (opt.isEmpty()) {
            return AdminAiResponseDTO.builder()
                    .reponse("❌ Dossier **" + numero + "** introuvable.")
                    .typeReponse("ERREUR")
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        try {
            String commentaire = "Action effectuée via l'assistant IA admin";
            dossierService.changerStatut(opt.get().getId(),
                    new ChangementStatutDTO(nouveauStatut, commentaire), adminId);

            return AdminAiResponseDTO.builder()
                    .reponse("✅ Dossier **" + numero + "** passé au statut **" + nouveauStatut + "** avec succès.")
                    .typeReponse("ACTION")
                    .action(AdminAiResponseDTO.ActionEffectuee.builder()
                            .type("CHANGEMENT_STATUT").nbDossiers(1)
                            .details(numero + " → " + nouveauStatut).succes(true).build())
                    .timestamp(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            return AdminAiResponseDTO.builder()
                    .reponse("❌ Impossible : " + e.getMessage())
                    .typeReponse("ERREUR")
                    .action(AdminAiResponseDTO.ActionEffectuee.builder()
                            .type("CHANGEMENT_STATUT").nbDossiers(0)
                            .details(e.getMessage()).succes(false).build())
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    private AdminAiResponseDTO traiterBulkAction(String message, UUID adminId) {
        StatutDossier source = extraireStatutSource(message);
        StatutDossier cible  = extraireStatut(message);

        if (source == null) source = StatutDossier.SOUMIS; // sensible default
        if (cible == null)  cible  = StatutDossier.EN_TRAITEMENT;

        int limite = extraireLimite(message, 50);

        var dossiers = dossierRepository
                .findByStatut(source, PageRequest.of(0, limite))
                .getContent();

        if (dossiers.isEmpty()) {
            return AdminAiResponseDTO.builder()
                    .reponse("Aucun dossier en statut **" + source + "** trouvé.")
                    .typeReponse("ANALYSE")
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        int succes = 0;
        List<String> erreurs = new ArrayList<>();
        String commentaire = "Traitement en lot via l'assistant IA admin";

        for (DossierImmatriculation d : dossiers) {
            try {
                dossierService.changerStatut(d.getId(),
                        new ChangementStatutDTO(cible, commentaire), adminId);
                succes++;
            } catch (Exception e) {
                erreurs.add(d.getNumeroDossier() + ": " + e.getMessage());
            }
        }

        String rapport = String.format(
                "✅ Traitement en lot : **%d/%d** dossiers passés **%s → %s**.%s",
                succes, dossiers.size(), source, cible,
                erreurs.isEmpty() ? "" : "\n⚠️ " + erreurs.size() + " erreur(s) : " + erreurs.get(0));

        return AdminAiResponseDTO.builder()
                .reponse(rapport)
                .typeReponse("ACTION")
                .action(AdminAiResponseDTO.ActionEffectuee.builder()
                        .type("BULK_UPDATE").nbDossiers(succes)
                        .details(source + " → " + cible).succes(erreurs.isEmpty()).build())
                .timestamp(LocalDateTime.now())
                .build();
    }

    private AdminAiResponseDTO traiterGeneral(String message) {
        String reponse = narerAvecLlm(message, "",
                "Tu es l'assistant IA de l'administrateur DGI. " +
                        "Réponds de façon professionnelle en français. " +
                        "Si la question concerne les données du système, " +
                        "dis à l'admin de reformuler avec des mots-clés comme " +
                        "'statistiques', 'dossier DGI-...', 'tendances', etc.");

        return AdminAiResponseDTO.builder()
                .reponse(reponse)
                .typeReponse("GENERAL")
                .timestamp(LocalDateTime.now())
                .build();
    }

    // -------------------------------------------------------------------------
    // LLM narration — receives real data, writes natural language answer
    // -------------------------------------------------------------------------

    private String narerAvecLlm(String messageAdmin, String donneesReelles, String instruction) {
        if (apiKey == null || apiKey.isBlank()) {
            return donneesReelles.isEmpty() ? "Service LLM non configuré." : donneesReelles;
        }

        String systemPrompt = """
                Tu es l'assistant IA de l'administrateur de la DGI tunisienne.
                Tu as accès aux données réelles du système fournies ci-dessous.
                RÈGLE CRITIQUE : utilise UNIQUEMENT les données fournies. Ne dis JAMAIS
                que tu n'as pas accès aux données — tu les as déjà dans ce message.
                Réponds en français, de façon professionnelle et concise.
                """ + instruction;

        String userContent = donneesReelles.isEmpty()
                ? messageAdmin
                : "DONNÉES DU SYSTÈME :\n" + donneesReelles + "\n\nDEMANDE DE L'ADMIN : " + messageAdmin;

        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userContent)
        );

        try {
            String response = restClient.post()
                    .uri("/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("model", modele, "max_tokens", 800,
                            "temperature", 0.4, "messages", messages))
                    .retrieve()
                    .onStatus(s -> s.isError(), (req, res) ->
                            log.warn("Admin AI LLM error: {}", res.getStatusCode()))
                    .body(String.class);

            if (response == null) return donneesReelles;
            JsonNode root = objectMapper.readTree(response);
            return root.path("choices").get(0).path("message").path("content").asText(donneesReelles);

        } catch (Exception e) {
            log.error("Admin AI LLM narration failed", e);
            return donneesReelles; // fallback: return raw data
        }
    }

    // -------------------------------------------------------------------------
    // Extraction helpers
    // -------------------------------------------------------------------------

    private String extraireNumeroDossier(String message) {
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("DGI-\\d{4}-\\d+", java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(message);
        return m.find() ? m.group().toUpperCase() : null;
    }

    private StatutDossier extraireStatut(String message) {
        String m = message.toLowerCase();
        if (contient(m, "en traitement", "traitement", "traiter", "assigner")) return StatutDossier.EN_TRAITEMENT;
        if (contient(m, "valide", "valider", "approuver", "accepter"))          return StatutDossier.VALIDE;
        if (contient(m, "rejette", "rejeter", "refuser", "rejet"))              return StatutDossier.REJETE;
        if (contient(m, "soumis", "soumettre"))                                 return StatutDossier.SOUMIS;
        if (contient(m, "attente contribuable", "info"))    return StatutDossier.EN_ATTENTE_CONTRIBUABLE;
        return null;
    }

    private StatutDossier extraireStatutSource(String message) {
        String m = message.toLowerCase();
        if (contient(m, "soumis"))        return StatutDossier.SOUMIS;
        if (contient(m, "brouillon"))     return StatutDossier.BROUILLON;
        if (contient(m, "en traitement")) return StatutDossier.EN_TRAITEMENT;
        return null;
    }

    private int extraireLimite(String message, int defaut) {
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("\\b(\\d+)\\b").matcher(message);
        while (m.find()) {
            int n = Integer.parseInt(m.group(1));
            if (n > 0 && n <= 500) return n;
        }
        return defaut;
    }
}