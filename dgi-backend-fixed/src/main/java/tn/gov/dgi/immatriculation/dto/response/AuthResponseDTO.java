package tn.gov.dgi.immatriculation.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.gov.dgi.immatriculation.model.Role;

import java.util.UUID;

/**
 * Réponse renvoyée par /api/auth/login, /register, /register-agent.
 *
 * BUG-2 FIX : l'ancienne version ne renvoyait que { token, email, role,
 * contribuableId }. Le frontend Angular (AuthService.mapUtilisateur) lisait
 * reponse.utilisateur?.id / .nom / .prenom → TOUS undefined →
 * session incomplète (initiales vides, id manquant).
 *
 * Version corrigée ajoute l'objet "utilisateur" imbriqué attendu par Angular.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponseDTO {

    /** JWT porteur — durée configurée par app.jwt.expiration-ms (24 h). */
    private String token;

    /** Email répété à la racine pour compatibilité ascendante. */
    private String email;

    /** Rôle : CONTRIBUABLE | AGENT_DGI | ADMIN. */
    private Role role;

    /**
     * ID du contribuable métier lié au compte.
     * Null pour AGENT_DGI / ADMIN.
     * Répété à la racine pour que les guards Angular puissent le lire
     * directement sans descendre dans l'objet utilisateur.
     */
    private UUID contribuableId;

    /**
     * Objet utilisateur imbriqué — lu par AuthService.mapUtilisateur()
     * dans le frontend Angular.
     * Contient les champs nécessaires à l'initialisation de la session
     * (id, nom, prénom pour les initiales et l'affichage du profil).
     *
     * BUG-2 FIX : champ manquant dans la version originale.
     */
    private UtilisateurDTO utilisateur;

    // ── Inner DTO ─────────────────────────────────────────────────────────────

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class UtilisateurDTO {
        private UUID   id;
        private String email;
        private String nom;
        private String prenom;
    }
}
