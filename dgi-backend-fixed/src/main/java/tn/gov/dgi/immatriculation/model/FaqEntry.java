package tn.gov.dgi.immatriculation.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "faq_entries")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class FaqEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "mots_cles", nullable = false, columnDefinition = "TEXT")
    private String motsCles; // "pièce,document,justificatif"

    @Column(name = "question", nullable = false, length = 500)
    private String question;

    @Column(name = "reponse", nullable = false, columnDefinition = "TEXT")
    private String reponse;

    @Column(name = "categorie", length = 50)
    private String categorie;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_cible", nullable = false, length = 20)
    @Builder.Default
    private RoleCibleFaq roleCible = RoleCibleFaq.TOUS;

    @Column(name = "actif", nullable = false)
    @Builder.Default
    private Boolean actif = true;

    @CreationTimestamp
    @Column(name = "date_creation", updatable = false)
    private LocalDateTime dateCreation;

    @Transient
    public List<String> getListeMotsCles() {
        return Arrays.stream(motsCles.split(","))
                .map(String::trim).map(String::toLowerCase).toList();
    }
}