package tn.gov.dgi.immatriculation.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.gov.dgi.immatriculation.dto.response.DossierResponseDTO;
import tn.gov.dgi.immatriculation.model.Role;
import tn.gov.dgi.immatriculation.repository.DossierImmatriculationRepository;
import tn.gov.dgi.immatriculation.repository.UtilisateurRepository;
import tn.gov.dgi.immatriculation.service.DossierService;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Détecte si le message concerne le statut d'un dossier et, si oui,
 * construit la réponse EXCLUSIVEMENT à partir des données réelles en base
 * (jamais via le LLM) — un statut de dossier officiel ne doit jamais être
 * une donnée "générée", même de façon plausible.
 */
@Service
@RequiredArgsConstructor
public class DossierStatusIntentService {

    private static final Pattern PATTERN_NUMERO_DOSSIER = Pattern.compile("DGI-\\d{4}-\\d{6}");
    private static final List<String> MOTS_CLES_INTENTION = List.of(
            "statut", "état", "avancement", "où en est", "suivi", "ou en est");

    private final DossierService dossierService;
    private final UtilisateurRepository utilisateurRepository;

    public boolean estUneDemandeDeStatut(String message) {
        String normalise = message.toLowerCase(Locale.ROOT);
        return MOTS_CLES_INTENTION.stream().anyMatch(normalise::contains)
                || PATTERN_NUMERO_DOSSIER.matcher(message.toUpperCase(Locale.ROOT)).find();
    }

    public String construireReponseStatut(String message, UUID utilisateurId, Role role) {
        Matcher matcher = PATTERN_NUMERO_DOSSIER.matcher(message.toUpperCase(Locale.ROOT));

        if (matcher.find()) {
            String numeroDossier = matcher.group();
            return formaterStatutParNumero(numeroDossier);
        }

        // Pas de numéro explicite : on regarde les dossiers du contribuable connecté
        if (role == Role.CONTRIBUABLE) {
            Optional<UUID> contribuableId = utilisateurRepository.findById(utilisateurId)
                    .map(u -> u.getContribuableId());

            if (contribuableId.isPresent()) {
                List<DossierResponseDTO> dossiers = dossierService.listerParContribuable(contribuableId.get());
                if (dossiers.isEmpty()) {
                    return "Vous n'avez aucun dossier enregistré pour le moment.";
                }
                DossierResponseDTO plusRecent = dossiers.get(0);
                return "Votre dossier le plus récent (%s) est actuellement au statut : %s.".formatted(
                        plusRecent.getNumeroDossier(), traduireStatut(plusRecent.getStatut().name()));
            }
        }

        return "Pourriez-vous préciser le numéro de votre dossier (format DGI-AAAA-NNNNNN) pour que je puisse vérifier son statut ?";
    }

    private String formaterStatutParNumero(String numeroDossier) {
        try {
            // Nécessite une méthode de recherche par numéro dans DossierService
            // (à ajouter : DossierResponseDTO obtenirParNumero(String numeroDossier))
            DossierResponseDTO dossier = dossierService.obtenirParNumero(numeroDossier);
            String base = "Le dossier %s est actuellement au statut : %s.".formatted(
                    numeroDossier, traduireStatut(dossier.getStatut().name()));
            if (dossier.getCommentaireAgent() != null && !dossier.getCommentaireAgent().isBlank()) {
                base += " Commentaire de l'agent : " + dossier.getCommentaireAgent();
            }
            return base;
        } catch (Exception e) {
            return "Aucun dossier trouvé avec le numéro " + numeroDossier + ".";
        }
    }

    private String traduireStatut(String statut) {
        return switch (statut) {
            case "BROUILLON" -> "en préparation (non soumis)";
            case "SOUMIS" -> "soumis, en attente de prise en charge";
            case "EN_TRAITEMENT" -> "en cours de traitement par un agent";
            case "VALIDE" -> "validé";
            case "REJETE" -> "rejeté";
            default -> statut;
        };
    }
}