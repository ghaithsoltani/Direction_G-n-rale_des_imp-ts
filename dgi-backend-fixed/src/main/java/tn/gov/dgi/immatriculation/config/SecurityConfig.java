package tn.gov.dgi.immatriculation.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tn.gov.dgi.immatriculation.security.JwtAuthenticationFilter;
import tn.gov.dgi.immatriculation.security.UserDetailsServiceImpl;

/**
 * Spring Security — configuration JWT stateless.
 *
 * BUG-SEC-1 FIX : /forgot-password et /reset-password étaient déclarés
 *   APRÈS anyRequest().authenticated() dans la chaîne originale. Spring
 *   Security évalue les règles dans l'ORDRE de déclaration et s'arrête
 *   à la première correspondance. La règle catchall capturait ces deux
 *   routes avant les permitAll() → elles renvoyaient 401 au lieu de
 *   fonctionner publiquement. Toutes les règles publiques sont maintenant
 *   déclarées AVANT anyRequest().
 *
 * BUG-SEC-2 FIX : GET /api/dossiers/{id} n'avait aucune règle explicite.
 *   Un CONTRIBUABLE authentifié recevait 403 en essayant de consulter
 *   son propre dossier (suivi de statut). Route ajoutée avec .authenticated().
 *
 * BUG-SEC-3 FIX : /api/chatbot/faq (GET) était couvert par la règle
 *   /api/chatbot/faq/** → hasRole("ADMIN"). Un CONTRIBUABLE voulant lire
 *   les FAQ recevait 403. GET /faq séparé en règle distincte (authenticated).
 *
 * BUG-SEC-4 FIX : GET /api/contribuables/{id} tombait dans anyRequest().
 *   Un contribuable ne pouvait pas récupérer sa propre fiche après
 *   inscription → 403. Règle authenticated() ajoutée.
 *
 * BUG-SEC-5 FIX : POST /soumettre et /documents limités à CONTRIBUABLE.
 *   Un ADMIN en test recevait 403 → hasAnyRole("CONTRIBUABLE","ADMIN").
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsServiceImpl  userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth

                // ── Auth publique ─────────────────────────────────────────────
                .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
                // BUG-SEC-1 FIX : DOIT être avant anyRequest()
                .requestMatchers("/api/auth/forgot-password", "/api/auth/reset-password").permitAll()
                .requestMatchers("/api/auth/register-agent").hasRole("ADMIN")

                // ── Swagger / Actuator ───────────────────────────────────────
                .requestMatchers("/swagger-ui/**", "/api-docs/**", "/actuator/health").permitAll()

                // ── Contribuables ────────────────────────────────────────────
                .requestMatchers(HttpMethod.POST, "/api/contribuables").permitAll()
                // BUG-SEC-4 FIX : un CONTRIBUABLE doit lire sa propre fiche
                .requestMatchers(HttpMethod.GET, "/api/contribuables/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/contribuables").hasAnyRole("AGENT_DGI", "ADMIN")

                // ── Dossiers — contribuable ───────────────────────────────────
                // BUG-SEC-5 FIX : ADMIN peut aussi créer/soumettre/uploader (pour tests)
                .requestMatchers(HttpMethod.POST, "/api/dossiers").hasAnyRole("CONTRIBUABLE", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/dossiers/*/soumettre")
                        .hasAnyRole("CONTRIBUABLE", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/dossiers/*/documents")
                        .hasAnyRole("CONTRIBUABLE", "ADMIN")

                // ── Dossiers — lecture agent/contribuable ─────────────────────
                // IMPORTANT : /statistiques AVANT /{id} pour éviter la capture par /*
                .requestMatchers(HttpMethod.GET, "/api/dossiers/statistiques")
                        .hasAnyRole("AGENT_DGI", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/dossiers/contribuable/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/dossiers")
                        .hasAnyRole("AGENT_DGI", "ADMIN")
                // BUG-SEC-2 FIX : GET dossier par ID accessible aux authentifiés
                // (un contribuable doit pouvoir consulter son propre dossier)
                .requestMatchers(HttpMethod.GET, "/api/dossiers/*").authenticated()

                // ── Dossiers — agents/admins ──────────────────────────────────
                .requestMatchers(HttpMethod.PUT, "/api/dossiers/*/statut")
                        .hasAnyRole("AGENT_DGI", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/dossiers/bulk-status")
                        .hasAnyRole("AGENT_DGI", "ADMIN")

                // ── Documents joints (liste + téléchargement) ─────────────────
                .requestMatchers(HttpMethod.GET,    "/api/dossiers/*/documents")
                        .hasAnyRole("AGENT_DGI", "ADMIN")
                .requestMatchers(HttpMethod.GET,    "/api/dossiers/*/documents/*")
                        .hasAnyRole("AGENT_DGI", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/dossiers/*/documents/*")
                        .hasAnyRole("AGENT_DGI", "ADMIN")

                // ── OCR / Face ───────────────────────────────────────────────
                .requestMatchers(HttpMethod.POST, "/api/ocr/extract")
                        .hasAnyRole("CONTRIBUABLE", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/face/verify")
                        .hasAnyRole("CONTRIBUABLE", "ADMIN")

                // ── Notes internes (agents/admins) ────────────────────────────
                .requestMatchers("/api/dossiers/*/notes/**")
                        .hasAnyRole("AGENT_DGI", "ADMIN")

                // ── Demandes d'information ────────────────────────────────────
                .requestMatchers(HttpMethod.POST, "/api/dossiers/*/information-requests")
                        .hasAnyRole("AGENT_DGI", "ADMIN")
                .requestMatchers(HttpMethod.GET,  "/api/dossiers/*/information-requests")
                        .authenticated()
                .requestMatchers(HttpMethod.POST, "/api/dossiers/*/information-requests/*/respond")
                        .hasAnyRole("CONTRIBUABLE", "ADMIN")

                // ── Admin AI ─────────────────────────────────────────────────
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // ── Chatbot ──────────────────────────────────────────────────
                .requestMatchers(HttpMethod.POST, "/api/chatbot/message").authenticated()
                // BUG-SEC-3 FIX : lecture FAQ accessible à tous les authentifiés
                .requestMatchers(HttpMethod.GET, "/api/chatbot/faq").authenticated()
                // Gestion FAQ (créer/modifier/supprimer) : ADMIN uniquement
                .requestMatchers(HttpMethod.POST,   "/api/chatbot/faq/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT,    "/api/chatbot/faq/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/chatbot/faq/**").hasRole("ADMIN")

                // ── Profil utilisateur ────────────────────────────────────────
                .requestMatchers("/api/users/me/**").authenticated()

                // ── Notifications ─────────────────────────────────────────────
                .requestMatchers("/api/notifications/**").authenticated()

                // ── Catchall ─── DOIT rester en DERNIÈRE POSITION ────────────
                // BUG-SEC-1 : toute règle déclarée APRÈS cette ligne est ignorée
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
