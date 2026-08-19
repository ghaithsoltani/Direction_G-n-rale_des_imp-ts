package tn.gov.dgi.immatriculation.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "utilisateurs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "mot_de_passe_hash", nullable = false)
    private String motDePasseHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;

    /** Renseigné uniquement pour role = CONTRIBUABLE, relie le compte au contribuable métier. */
    @Column(name = "contribuable_id")
    private UUID contribuableId;

    @Column(name = "actif", nullable = false)
    @Builder.Default
    private Boolean actif = true;

    @CreationTimestamp
    @Column(name = "date_creation", updatable = false)
    private LocalDateTime dateCreation;

    /** Token de réinitialisation de mot de passe (UUID aléatoire, usage unique). */
    @Column(name = "reset_token", length = 100)
    private String resetToken;

    /** Date d'expiration du token (1 heure après émission). */
    @Column(name = "reset_token_expiry")
    private LocalDateTime resetTokenExpiry;

    // Profile fields (V7)
    @Column(name = "prenom", length = 100)
    private String prenom;

    @Column(name = "nom", length = 100)
    private String nom;

    @Column(name = "telephone", length = 30)
    private String telephone;

    @Column(name = "langue_preferee", length = 5)
    @Builder.Default
    private String languePreferee = "fr";

    @Column(name = "notif_email")
    @Builder.Default
    private boolean notifEmail = true;

    @Column(name = "notif_app")
    @Builder.Default
    private boolean notifApp = true;
}
