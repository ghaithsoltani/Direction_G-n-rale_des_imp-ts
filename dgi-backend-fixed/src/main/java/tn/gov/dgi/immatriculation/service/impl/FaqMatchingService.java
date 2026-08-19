package tn.gov.dgi.immatriculation.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.gov.dgi.immatriculation.model.FaqEntry;
import tn.gov.dgi.immatriculation.model.Role;
import tn.gov.dgi.immatriculation.model.RoleCibleFaq;
import tn.gov.dgi.immatriculation.repository.FaqEntryRepository;

import java.util.*;

/**
 * FAQ retrieval service — upgraded to return top-K ranked results
 * for injection into the LLM prompt (RAG pattern).
 *
 * Ranking: keyword overlap score (count of matching keywords).
 * Simple and deterministic — no embedding model required.
 * For a FAQ of 20-100 entries this is sufficient and auditable.
 */
@Service
@RequiredArgsConstructor
public class FaqMatchingService {

    private static final int SCORE_MINIMUM = 1;

    private final FaqEntryRepository faqEntryRepository;

    /**
     * Returns the single best FAQ match (backward-compatible).
     */
    public Optional<FaqEntry> trouverMeilleureCorrespondance(String message, Role role) {
        List<FaqEntry> top = trouverTopK(message, role, 1);
        return top.isEmpty() ? Optional.empty() : Optional.of(top.get(0));
    }

    /**
     * Returns the top-K FAQ entries ranked by keyword overlap score.
     * Used by the RAG pipeline to build the LLM grounding context.
     *
     * @param message the user message (normalised internally)
     * @param role    the caller's role (filters by roleCible)
     * @param k       maximum number of results to return
     */
    public List<FaqEntry> trouverTopK(String message, Role role, int k) {
        List<RoleCibleFaq> rolesAcceptes = (role == Role.AGENT_DGI || role == Role.ADMIN)
                ? List.of(RoleCibleFaq.TOUS, RoleCibleFaq.AGENT_DGI)
                : List.of(RoleCibleFaq.TOUS, RoleCibleFaq.CONTRIBUABLE);

        String messageNormalise = message.toLowerCase(Locale.ROOT);

        record ScoredEntry(FaqEntry entry, int score) {}

        return faqEntryRepository.findByActifTrueAndRoleCibleIn(rolesAcceptes)
                .stream()
                .map(entry -> {
                    int score = (int) entry.getListeMotsCles().stream()
                            .filter(messageNormalise::contains)
                            .count();
                    return new ScoredEntry(entry, score);
                })
                .filter(se -> se.score() >= SCORE_MINIMUM)
                .sorted(Comparator.comparingInt(ScoredEntry::score).reversed())
                .limit(k)
                .map(ScoredEntry::entry)
                .toList();
    }
}