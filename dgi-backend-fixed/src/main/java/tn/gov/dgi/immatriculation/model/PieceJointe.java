package tn.gov.dgi.immatriculation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Table dédiée, FK vers dossier_id.
 *
 * Stockage du fichier binaire : PostgreSQL n'a pas d'équivalent direct de
 * GridFS. Deux options (détaillées à l'étape 9 - configuration) :
 *   a) Colonne "bytea" contenant le fichier directement en base
 *      (simple, mais grossit la base rapidement, pas idéal pour de gros PDFs)
 *   b) Fichier stocké sur disque (ou object storage type MinIO/S3),
 *      seule "cheminStockage" enregistrée ici (RECOMMANDÉ, cf. contrainte
 *      initiale "système de fichiers local avec référence en base")
 * On retient l'option (b) par défaut, "cheminStockage" ci-dessous.
 */
@Entity
@Table(name = "pieces_jointes", indexes = {
        @Index(name = "idx_piece_dossier", columnList = "dossier_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PieceJointe {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dossier_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_piece_dossier"))
    private DossierImmatriculation dossier;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_piece", nullable = false, length = 30)
    private TypePieceJointe typePiece;

    @Column(name = "nom_fichier_original", nullable = false, length = 255)
    private String nomFichierOriginal;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "taille_octets")
    private Long tailleOctets;

    /**
     * Chemin relatif ou clé objet vers le fichier stocké sur disque/object
     * storage. Remplace le "gridFsId" de la version MongoDB.
     */
    @Column(name = "chemin_stockage", nullable = false, length = 500)
    private String cheminStockage;

    // Résultat OCR : fusionné dans cette même table
    @Embedded
    private ResultatOcr resultatOcr;

    @CreationTimestamp
    @Column(name = "date_upload", updatable = false, nullable = false)
    private LocalDateTime dateUpload;
}