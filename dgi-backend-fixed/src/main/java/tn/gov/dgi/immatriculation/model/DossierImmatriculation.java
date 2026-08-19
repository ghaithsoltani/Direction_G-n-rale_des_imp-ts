package tn.gov.dgi.immatriculation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Choix de modélisation :
 * - contribuable : @ManyToOne avec FK réelle. Contrairement à Mongo (où on
 *   stockait un simple String id résolu manuellement), ici la base garantit
 *   nativement qu'un dossier ne peut pas référencer un contribuable
 *   inexistant. FetchType.LAZY pour éviter de charger le contribuable
 *   complet à chaque requête sur la liste des dossiers (utile pour l'écran
 *   agent DGI qui liste beaucoup de dossiers).
 * - piecesJointes : @OneToMany, FK portée par PieceJointe (mappedBy) —
 *   cascade limitée à PERSIST/MERGE, PAS à REMOVE automatique implicite
 *   (suppression gérée explicitement en service pour respecter l'archivage
 *   réglementaire).
 * - historiqueStatuts : @ElementCollection -> table "historique_statuts"
 *   séparée, FK vers dossier_id, ordonnée par date.
 * - resultatVerificationFaciale : @Embeddable, fusionné dans cette table
 *   (un seul résultat par dossier, toujours consulté avec lui).
 */
@Entity
@Table(name = "dossiers_immatriculation", indexes = {
        @Index(name = "idx_dossier_statut", columnList = "statut"),
        @Index(name = "idx_dossier_contribuable", columnList = "contribuable_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DossierImmatriculation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "numero_dossier", unique = true, nullable = false, length = 30)
    private String numeroDossier; // ex: DGI-2026-000123

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contribuable_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_dossier_contribuable"))
    private Contribuable contribuable;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    @Builder.Default
    private StatutDossier statut = StatutDossier.BROUILLON;

    @OneToMany(mappedBy = "dossier", cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            orphanRemoval = false, fetch = FetchType.LAZY)
    @Builder.Default
    private List<PieceJointe> piecesJointes = new ArrayList<>();

    /**
     * Table "historique_statuts" générée automatiquement par Hibernate,
     * avec FK "dossier_id" vers cette table.
     */
    @ElementCollection
    @CollectionTable(
            name = "historique_statuts",
            joinColumns = @JoinColumn(name = "dossier_id",
                    foreignKey = @ForeignKey(name = "fk_historique_dossier"))
    )
    @OrderColumn(name = "ordre")
    @Builder.Default
    private List<HistoriqueStatut> historiqueStatuts = new ArrayList<>();

    // Résultat de vérification faciale : fusionné dans cette table
    @Embedded
    private ResultatVerificationFaciale resultatVerificationFaciale;

    @Column(name = "commentaire_agent", length = 1000)
    private String commentaireAgent;

    @Column(name = "agent_traitant_id")
    private UUID agentTraitantId;

    @Column(name = "priorite", length = 10)
    @Builder.Default
    private String priorite = "NORMALE"; // BASSE, NORMALE, HAUTE, URGENTE

    @Column(name = "deadline")
    private LocalDateTime deadline;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @CreationTimestamp
    @Column(name = "date_creation", updatable = false, nullable = false)
    private LocalDateTime dateCreation;

    @UpdateTimestamp
    @Column(name = "date_derniere_modification", nullable = false)
    private LocalDateTime dateDerniereModification;

    @Column(name = "date_soumission")
    private LocalDateTime dateSoumission;
}