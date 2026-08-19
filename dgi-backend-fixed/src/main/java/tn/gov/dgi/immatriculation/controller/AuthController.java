package tn.gov.dgi.immatriculation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import tn.gov.dgi.immatriculation.dto.request.LoginRequestDTO;
import tn.gov.dgi.immatriculation.dto.request.RegisterRequestDTO;
import tn.gov.dgi.immatriculation.dto.response.AuthResponseDTO;
import tn.gov.dgi.immatriculation.dto.response.AuthResponseDTO.UtilisateurDTO;
import tn.gov.dgi.immatriculation.exception.DocumentInvalideException;
import tn.gov.dgi.immatriculation.model.Role;
import tn.gov.dgi.immatriculation.model.Utilisateur;
import tn.gov.dgi.immatriculation.repository.UtilisateurRepository;
import tn.gov.dgi.immatriculation.security.JwtService;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Endpoints d'authentification.
 *
 * BUG-1 FIX : /forgot-password et /reset-password étaient déclarés publics
 *   dans SecurityConfig mais ABSENTS de ce contrôleur → 404 systématique
 *   depuis le frontend Angular. Les deux endpoints sont maintenant implémentés.
 *
 * BUG-2 FIX : Toutes les réponses auth incluent maintenant l'objet
 *   "utilisateur" imbriqué (id, email, nom, prenom) attendu par
 *   AuthService.mapUtilisateur() côté Angular. Sans ce champ la session
 *   était incomplète (initiales vides, id undefined).
 *
 * BUG-4 FIX : DisabledException (compte désactivé) géré explicitement
 *   pour éviter un 500 générique.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder       passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService            jwtService;

    // ── POST /api/auth/register  (public — CONTRIBUABLE) ─────────────────────
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> inscription(@Valid @RequestBody RegisterRequestDTO dto) {
        if (utilisateurRepository.existsByEmail(dto.getEmail())) {
            throw new DocumentInvalideException("Un compte existe déjà avec cet email.");
        }

        Utilisateur utilisateur = Utilisateur.builder()
                .email(dto.getEmail())
                .motDePasseHash(passwordEncoder.encode(dto.getMotDePasse()))
                .role(Role.CONTRIBUABLE)
                .contribuableId(dto.getContribuableId())
                .actif(true)
                .build();

        Utilisateur saved = utilisateurRepository.save(utilisateur);
        // BUG-2 FIX : réponse avec objet utilisateur imbriqué
        return ResponseEntity.ok(construireReponse(jwtService.genererToken(saved), saved));
    }

    // ── POST /api/auth/login  (public) ────────────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> connexion(@Valid @RequestBody LoginRequestDTO dto) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getMotDePasse()));
        } catch (DisabledException e) {
            // BUG-4 FIX : message métier clair au lieu d'un 500 générique
            throw new DocumentInvalideException(
                    "Ce compte a été désactivé. Veuillez contacter l'administration.");
        } catch (BadCredentialsException e) {
            throw new DocumentInvalideException("Identifiants invalides.");
        }

        Utilisateur utilisateur = utilisateurRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new DocumentInvalideException("Identifiants invalides."));

        // BUG-2 FIX : réponse enrichie avec utilisateur imbriqué
        return ResponseEntity.ok(construireReponse(jwtService.genererToken(utilisateur), utilisateur));
    }

    // ── POST /api/auth/register-agent  (ADMIN uniquement — cf. SecurityConfig) ─
    @PostMapping("/register-agent")
    public ResponseEntity<AuthResponseDTO> inscrireAgent(@Valid @RequestBody RegisterRequestDTO dto) {
        if (utilisateurRepository.existsByEmail(dto.getEmail())) {
            throw new DocumentInvalideException("Un compte existe déjà avec cet email.");
        }

        Utilisateur utilisateur = Utilisateur.builder()
                .email(dto.getEmail())
                .motDePasseHash(passwordEncoder.encode(dto.getMotDePasse()))
                .role(Role.AGENT_DGI)
                .actif(true)
                .build();

        Utilisateur saved = utilisateurRepository.save(utilisateur);
        return ResponseEntity.ok(construireReponse(jwtService.genererToken(saved), saved));
    }

    // ── POST /api/auth/forgot-password  (public) ─────────────────────────────
    /**
     * BUG-1 FIX : endpoint manquant → 404 depuis le frontend Angular.
     *
     * La réponse est intentionnellement identique que le compte existe ou non
     * (protection anti-énumération de comptes).
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> motDePasseOublie(
            @RequestBody Map<String, String> body) {

        String email = body.get("email");
        if (email == null || email.isBlank()) {
            throw new DocumentInvalideException("L'adresse e-mail est obligatoire.");
        }

        utilisateurRepository.findByEmail(email.trim().toLowerCase()).ifPresent(u -> {
            String resetToken = UUID.randomUUID().toString();
            u.setResetToken(resetToken);
            u.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
            utilisateurRepository.save(u);

            // TODO (production) : envoyer l'e-mail via JavaMailSender / SendGrid
            // Lien attendu par Angular :
            //   http://localhost:4200/auth/reset-password?token={resetToken}
            log.info("[DEV] Lien de réinitialisation pour {} → /auth/reset-password?token={}",
                    email, resetToken);
        });

        return ResponseEntity.ok(Map.of(
                "message",
                "Si un compte est associé à cet email, vous recevrez un lien de réinitialisation."
        ));
    }

    // ── POST /api/auth/reset-password  (public) ──────────────────────────────
    /**
     * BUG-1 FIX : endpoint manquant → 404 depuis le frontend Angular.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> reinitialiserMotDePasse(
            @RequestBody Map<String, String> body) {

        String token      = body.get("token");
        String motDePasse = body.get("motDePasse");

        if (token == null || token.isBlank()) {
            throw new DocumentInvalideException("Le token de réinitialisation est obligatoire.");
        }
        if (motDePasse == null || motDePasse.length() < 8) {
            throw new DocumentInvalideException(
                    "Le mot de passe doit contenir au moins 8 caractères.");
        }

        Utilisateur utilisateur = utilisateurRepository.findByResetToken(token)
                .orElseThrow(() -> new DocumentInvalideException(
                        "Lien invalide ou déjà utilisé."));

        if (utilisateur.getResetTokenExpiry() == null
                || utilisateur.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new DocumentInvalideException(
                    "Ce lien a expiré (valable 1 heure). Veuillez refaire la demande.");
        }

        utilisateur.setMotDePasseHash(passwordEncoder.encode(motDePasse));
        utilisateur.setResetToken(null);        // usage unique — invalide immédiatement
        utilisateur.setResetTokenExpiry(null);
        utilisateurRepository.save(utilisateur);

        return ResponseEntity.ok(Map.of("message", "Mot de passe réinitialisé avec succès."));
    }

    // ── Helper : construit une AuthResponseDTO complète (BUG-2 FIX) ──────────
    private AuthResponseDTO construireReponse(String token, Utilisateur u) {
        UtilisateurDTO utilisateurDTO = UtilisateurDTO.builder()
                .id(u.getId())
                .email(u.getEmail())
                .nom(u.getNom())
                .prenom(u.getPrenom())
                .build();

        return AuthResponseDTO.builder()
                .token(token)
                .email(u.getEmail())
                .role(u.getRole())
                .contribuableId(u.getContribuableId())
                .utilisateur(utilisateurDTO)
                .build();
    }
}
