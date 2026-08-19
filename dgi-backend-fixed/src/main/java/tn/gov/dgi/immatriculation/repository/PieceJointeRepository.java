package tn.gov.dgi.immatriculation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.gov.dgi.immatriculation.model.PieceJointe;
import tn.gov.dgi.immatriculation.model.TypePieceJointe;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PieceJointeRepository extends JpaRepository<PieceJointe, UUID> {

    @Query("SELECT p FROM PieceJointe p WHERE p.dossier.id = :dossierId")
    List<PieceJointe> findByDossierId(@Param("dossierId") UUID dossierId);

    @Query("SELECT p FROM PieceJointe p WHERE p.dossier.id = :dossierId ORDER BY p.dateUpload ASC")
    List<PieceJointe> findByDossierIdOrderByDateUploadAsc(@Param("dossierId") UUID dossierId);

    @Query("SELECT p FROM PieceJointe p WHERE p.dossier.id = :dossierId AND p.typePiece = :typePiece")
    Optional<PieceJointe> findByDossierIdAndTypePiece(
            @Param("dossierId") UUID dossierId,
            @Param("typePiece") TypePieceJointe typePiece);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM PieceJointe p WHERE p.dossier.id = :dossierId AND p.typePiece = :typePiece")
    boolean existsByDossierIdAndTypePiece(
            @Param("dossierId") UUID dossierId,
            @Param("typePiece") TypePieceJointe typePiece);

    @Query("DELETE FROM PieceJointe p WHERE p.dossier.id = :dossierId AND p.typePiece = :typePiece")
    void deleteByDossierIdAndTypePiece(
            @Param("dossierId") UUID dossierId,
            @Param("typePiece") TypePieceJointe typePiece);
}