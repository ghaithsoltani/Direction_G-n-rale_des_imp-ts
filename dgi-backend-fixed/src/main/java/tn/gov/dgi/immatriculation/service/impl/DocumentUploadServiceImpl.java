package tn.gov.dgi.immatriculation.service.impl;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tn.gov.dgi.immatriculation.dto.response.PieceJointeResponseDTO;
import tn.gov.dgi.immatriculation.exception.DocumentInvalideException;
import tn.gov.dgi.immatriculation.exception.DossierNotFoundException;
import tn.gov.dgi.immatriculation.mapper.PieceJointeMapper;
import tn.gov.dgi.immatriculation.model.DossierImmatriculation;
import tn.gov.dgi.immatriculation.model.PieceJointe;
import tn.gov.dgi.immatriculation.model.TypePieceJointe;
import tn.gov.dgi.immatriculation.repository.DossierImmatriculationRepository;
import tn.gov.dgi.immatriculation.repository.PieceJointeRepository;
import tn.gov.dgi.immatriculation.service.DocumentUploadService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DocumentUploadServiceImpl implements DocumentUploadService {

    private static final Logger log = LoggerFactory.getLogger(DocumentUploadServiceImpl.class);

    private static final Set<String> CONTENT_TYPES_AUTORISES = Set.of(
            "image/jpeg", "image/png", "application/pdf");
    private static final long TAILLE_MAX_OCTETS = 10 * 1024 * 1024; // 10 Mo

    private final PieceJointeRepository pieceJointeRepository;
    private final DossierImmatriculationRepository dossierRepository;
    private final PieceJointeMapper pieceJointeMapper;

    @Value("${app.stockage.chemin-racine}")
    private String cheminRacine;

    @PostConstruct
    public void init() {
        log.info("========================================");
        log.info("Chemin racine configuré: {}", cheminRacine);
        Path path = Paths.get(cheminRacine);
        log.info("Répertoire existe: {}", Files.exists(path));
        log.info("Répertoire writable: {}", Files.isWritable(path));
        log.info("========================================");
    }

    @Override
    public PieceJointeResponseDTO uploaderDocument(UUID dossierId, TypePieceJointe typePiece, MultipartFile fichier) {
        validerFichier(fichier);

        DossierImmatriculation dossier = dossierRepository.findById(dossierId)
                .orElseThrow(() -> new DossierNotFoundException("Aucun dossier trouvé avec l'id " + dossierId));

        String cheminRelatif = enregistrerSurDisque(dossierId, fichier);

        PieceJointe piece = PieceJointe.builder()
                .dossier(dossier)
                .typePiece(typePiece)
                .nomFichierOriginal(fichier.getOriginalFilename())
                .contentType(fichier.getContentType())
                .tailleOctets(fichier.getSize())
                .cheminStockage(cheminRelatif)
                .build();

        PieceJointe saved = pieceJointeRepository.save(piece);
        PieceJointeResponseDTO responseDTO = pieceJointeMapper.toDto(saved);
        responseDTO.setUrlTelechargement("/api/dossiers/" + dossierId + "/documents/" + saved.getId());
        return responseDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public Resource telechargerDocument(UUID pieceJointeId) {
        PieceJointe piece = trouverPieceOuLeverException(pieceJointeId);
        Path chemin = Paths.get(cheminRacine).resolve(piece.getCheminStockage());

        if (!Files.exists(chemin)) {
            log.error("Fichier physique manquant sur le disque pour la pièce {} : {}", pieceJointeId, chemin);
            throw new DocumentInvalideException("Le fichier associé à cette pièce jointe est introuvable");
        }
        return new FileSystemResource(chemin);
    }

    @Override
    @Transactional(readOnly = true)
    public PieceJointeResponseDTO obtenirMetadonnees(UUID pieceJointeId) {
        return pieceJointeMapper.toDto(trouverPieceOuLeverException(pieceJointeId));
    }

    @Override
    public void supprimerDocument(UUID pieceJointeId) {
        PieceJointe piece = trouverPieceOuLeverException(pieceJointeId);
        Path chemin = Paths.get(cheminRacine).resolve(piece.getCheminStockage());
        try {
            Files.deleteIfExists(chemin);
        } catch (IOException e) {
            log.warn("Impossible de supprimer le fichier physique {} : {}", chemin, e.getMessage());
        }
        pieceJointeRepository.delete(piece);
    }

    private void validerFichier(MultipartFile fichier) {
        if (fichier == null || fichier.isEmpty()) {
            throw new DocumentInvalideException("Le fichier envoyé est vide");
        }
        if (!CONTENT_TYPES_AUTORISES.contains(fichier.getContentType())) {
            throw new DocumentInvalideException(
                    "Type de fichier non autorisé : " + fichier.getContentType()
                            + ". Formats acceptés : JPEG, PNG, PDF");
        }
        if (fichier.getSize() > TAILLE_MAX_OCTETS) {
            throw new DocumentInvalideException("Le fichier dépasse la taille maximale autorisée (10 Mo)");
        }
    }

    private String enregistrerSurDisque(UUID dossierId, MultipartFile fichier) {
        try {
            String extension = extraireExtension(fichier.getOriginalFilename());
            String nomFichierPhysique = UUID.randomUUID() + extension;
            Path dossierCible = Paths.get(cheminRacine, dossierId.toString());

            log.debug("Création du répertoire: {}", dossierCible);
            Files.createDirectories(dossierCible);

            Path cheminComplet = dossierCible.resolve(nomFichierPhysique);
            log.debug("Enregistrement du fichier: {}", cheminComplet);
            Files.copy(fichier.getInputStream(), cheminComplet, StandardCopyOption.REPLACE_EXISTING);

            return dossierId + "/" + nomFichierPhysique;
        } catch (IOException e) {
            log.error("Erreur détaillée lors de l'enregistrement: ", e);  // Log full stack trace
            throw new DocumentInvalideException("Erreur lors de l'enregistrement du fichier : " + e.getMessage());
        }
    }

    private String extraireExtension(String nomFichier) {
        if (nomFichier == null || !nomFichier.contains(".")) {
            return "";
        }
        return nomFichier.substring(nomFichier.lastIndexOf('.'));
    }

    private PieceJointe trouverPieceOuLeverException(UUID pieceJointeId) {
        PieceJointe piece = pieceJointeRepository.findById(pieceJointeId)
                .orElseThrow(() -> new DocumentInvalideException(
                        "Aucune pièce jointe trouvée avec l'id " + pieceJointeId));
        // FIX: initialize lazy dossier reference so mapper can read dossier.id
        if (piece.getDossier() != null) {
            piece.getDossier().getId();
        }
        return piece;
    }
}