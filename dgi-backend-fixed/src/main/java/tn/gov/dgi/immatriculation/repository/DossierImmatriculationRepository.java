package tn.gov.dgi.immatriculation.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.gov.dgi.immatriculation.model.DossierImmatriculation;
import tn.gov.dgi.immatriculation.model.StatutDossier;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DossierImmatriculationRepository extends JpaRepository<DossierImmatriculation, UUID> {

    Optional<DossierImmatriculation> findByNumeroDossier(String numeroDossier);

    boolean existsByNumeroDossier(String numeroDossier);

    List<DossierImmatriculation> findByContribuableIdOrderByDateCreationDesc(UUID contribuableId);

    Page<DossierImmatriculation> findByStatut(StatutDossier statut, Pageable pageable);

    @Query(value = """
            SELECT * FROM dossiers_immatriculation
            WHERE (CAST(:statut AS VARCHAR) IS NULL OR statut = CAST(:statut AS VARCHAR))
            AND   (CAST(:dateDebut AS TIMESTAMP) IS NULL OR date_soumission >= CAST(:dateDebut AS TIMESTAMP))
            AND   (CAST(:dateFin   AS TIMESTAMP) IS NULL OR date_soumission <= CAST(:dateFin   AS TIMESTAMP))
            ORDER BY date_soumission ASC NULLS LAST
            """,
            countQuery = """
            SELECT COUNT(*) FROM dossiers_immatriculation
            WHERE (CAST(:statut AS VARCHAR) IS NULL OR statut = CAST(:statut AS VARCHAR))
            AND   (CAST(:dateDebut AS TIMESTAMP) IS NULL OR date_soumission >= CAST(:dateDebut AS TIMESTAMP))
            AND   (CAST(:dateFin   AS TIMESTAMP) IS NULL OR date_soumission <= CAST(:dateFin   AS TIMESTAMP))
            """,
            nativeQuery = true)
    Page<DossierImmatriculation> rechercherAvecFiltres(
            @Param("statut")    String statut,
            @Param("dateDebut") LocalDateTime dateDebut,
            @Param("dateFin")   LocalDateTime dateFin,
            Pageable pageable);

    long countByStatut(StatutDossier statut);

    Page<DossierImmatriculation> findByAgentTraitantId(UUID agentTraitantId, Pageable pageable);

    @Query("""
            SELECT d FROM DossierImmatriculation d
            WHERE d.numeroDossier LIKE CONCAT('DGI-', :annee, '-%')
            ORDER BY d.numeroDossier DESC
            """)
    List<DossierImmatriculation> findDerniersDossiersParAnnee(
            @Param("annee") String annee, Pageable pageable);

    /**
     * FIX 2: FUNCTION('MONTH'/'YEAR', ...) est une syntaxe MySQL/H2.
     * PostgreSQL utilise EXTRACT(MONTH FROM ...) / EXTRACT(YEAR FROM ...).
     * On utilise une native query pour avoir la syntaxe PostgreSQL exacte.
     */
    @Query(value = """
            SELECT EXTRACT(MONTH FROM date_creation)::int AS mois,
                   COUNT(id)                              AS total
            FROM   dossiers_immatriculation
            WHERE  EXTRACT(YEAR FROM date_creation) = :annee
            GROUP  BY mois
            ORDER  BY mois ASC
            """, nativeQuery = true)
    List<Object[]> countParMois(@Param("annee") int annee);
}