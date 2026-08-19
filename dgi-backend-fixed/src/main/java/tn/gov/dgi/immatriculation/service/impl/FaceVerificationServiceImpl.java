package tn.gov.dgi.immatriculation.service.impl;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.FloatPointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tn.gov.dgi.immatriculation.dto.response.FaceVerificationResponseDTO;
import tn.gov.dgi.immatriculation.exception.DossierNotFoundException;
import tn.gov.dgi.immatriculation.exception.VerificationFacialeEchoueeException;
import tn.gov.dgi.immatriculation.mapper.DossierMapper;
import tn.gov.dgi.immatriculation.model.DossierImmatriculation;
import tn.gov.dgi.immatriculation.model.PieceJointe;
import tn.gov.dgi.immatriculation.model.ResultatVerificationFaciale;
import tn.gov.dgi.immatriculation.repository.DossierImmatriculationRepository;
import tn.gov.dgi.immatriculation.repository.PieceJointeRepository;
import tn.gov.dgi.immatriculation.service.FaceVerificationService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.bytedeco.opencv.global.opencv_core.NORM_MINMAX;
import static org.bytedeco.opencv.global.opencv_core.normalize;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imdecode;
import static org.bytedeco.opencv.global.opencv_imgcodecs.IMREAD_GRAYSCALE;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

@Service
@RequiredArgsConstructor
@Transactional
public class FaceVerificationServiceImpl implements FaceVerificationService {

    private static final Logger log = LoggerFactory.getLogger(FaceVerificationServiceImpl.class);

    private final DossierImmatriculationRepository dossierRepository;
    private final PieceJointeRepository pieceJointeRepository;
    private final DossierMapper dossierMapper;

    @Value("${app.stockage.chemin-racine}")
    private String cheminRacine;

    @Value("${app.face-verification.seuil-acceptation:0.75}")
    private Double seuilAcceptation;

    @Value("${app.face-verification.cascade-path}")
    private String cheminCascadeVisage;

    @PostConstruct
    public void init() {
        log.info("========================================");
        log.info("Chemin cascade: {}", cheminCascadeVisage);
        log.info("Fichier existe: {}", Files.exists(Paths.get(cheminCascadeVisage)));
        log.info("========================================");
    }

    @Override
    public FaceVerificationResponseDTO verifier(UUID dossierId, UUID pieceJointeReferenceId, MultipartFile photoLive) {
        DossierImmatriculation dossier = dossierRepository.findById(dossierId)
                .orElseThrow(() -> new DossierNotFoundException("Aucun dossier trouvé avec l'id " + dossierId));

        PieceJointe pieceReference = pieceJointeRepository.findById(pieceJointeReferenceId)
                .orElseThrow(() -> new VerificationFacialeEchoueeException(
                        "Pièce de référence introuvable : " + pieceJointeReferenceId));

        try {
            Mat visageReference = extraireVisage(chargerImageDepuisDisque(pieceReference));
            Mat visageLive = extraireVisage(photoLive.getBytes());

            double score = comparerVisages(visageReference, visageLive);
            boolean correspondanceValidee = score >= seuilAcceptation;

            ResultatVerificationFaciale resultat = ResultatVerificationFaciale.builder()
                    .pieceJointeReferenceId(pieceJointeReferenceId)
                    .scoreSimilarite(score)
                    .seuilAcceptation(seuilAcceptation)
                    .correspondanceValidee(correspondanceValidee)
                    .dateVerification(LocalDateTime.now())
                    .build();

            dossier.setResultatVerificationFaciale(resultat);
            dossierRepository.save(dossier);

            return dossierMapper.toDto(resultat);

        } catch (IOException e) {
            throw new VerificationFacialeEchoueeException(
                    "Erreur de lecture des images : " + e.getMessage());
        } catch (VisageNonDetecteException e) {
            ResultatVerificationFaciale resultat = ResultatVerificationFaciale.builder()
                    .pieceJointeReferenceId(pieceJointeReferenceId)
                    .correspondanceValidee(false)
                    .dateVerification(LocalDateTime.now())
                    .messageErreur(e.getMessage())
                    .build();
            dossier.setResultatVerificationFaciale(resultat);
            dossierRepository.save(dossier);
            return dossierMapper.toDto(resultat);
        }
    }

    private byte[] chargerImageDepuisDisque(PieceJointe piece) throws IOException {
        Path chemin = Paths.get(cheminRacine).resolve(piece.getCheminStockage());
        return Files.readAllBytes(chemin);
    }

    private Mat extraireVisage(byte[] imageBytes) {
        // Load OpenCV native libraries
        Loader.load(org.bytedeco.opencv.global.opencv_objdetect.class);

        Mat buffer = new Mat(new BytePointer(imageBytes));
        Mat image = imdecode(buffer, IMREAD_GRAYSCALE);

        if (image.empty()) {
            throw new VisageNonDetecteException("Image illisible ou corrompue");
        }

        // FIX 5: OpenCV CascadeClassifier needs a native filesystem path, NOT a file:// URI.
        // The previous file:// prefix caused the classifier to silently fail to load.
        CascadeClassifier classifieur = new CascadeClassifier(cheminCascadeVisage);

        if (classifieur.empty()) {
            log.error("Échec du chargement du classifieur Haar: {}", cheminCascadeVisage);
            throw new VisageNonDetecteException("Échec du chargement du détecteur de visage");
        }

        log.debug("Classifieur chargé avec succès");

        RectVector visages = new RectVector();
        classifieur.detectMultiScale(image, visages);

        log.debug("Visages détectés: {}", visages.size());

        if (visages.size() == 0) {
            throw new VisageNonDetecteException("Aucun visage détecté sur l'image");
        }

        Rect premierVisage = visages.get(0);
        Mat visageRecadre = new Mat(image, premierVisage);
        Mat visageRedimensionne = new Mat();
        resize(visageRecadre, visageRedimensionne, new Size(200, 200));
        return visageRedimensionne;
    }

    private double comparerVisages(Mat visage1, Mat visage2) {
        try {
            Mat hist1 = new Mat();
            Mat hist2 = new Mat();

            int[] channels = {0};
            int[] histSize = {256};
            float[] ranges = {0f, 256f};

            calcHist(new MatVector(visage1), new IntPointer(channels), new Mat(), hist1,
                    new IntPointer(histSize), new FloatPointer(ranges), false);
            calcHist(new MatVector(visage2), new IntPointer(channels), new Mat(), hist2,
                    new IntPointer(histSize), new FloatPointer(ranges), false);

            normalize(hist1, hist1, 0, 1, NORM_MINMAX, -1, new Mat());
            normalize(hist2, hist2, 0, 1, NORM_MINMAX, -1, new Mat());

            return compareHist(hist1, hist2, HISTCMP_CORREL);
        } catch (Exception e) {
            log.error("Erreur lors de la comparaison des visages: ", e);
            return 0.0;
        }
    }

    private static class VisageNonDetecteException extends RuntimeException {
        VisageNonDetecteException(String message) { super(message); }
    }
}
