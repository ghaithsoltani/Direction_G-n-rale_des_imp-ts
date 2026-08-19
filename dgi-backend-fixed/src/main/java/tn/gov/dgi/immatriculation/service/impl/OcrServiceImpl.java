package tn.gov.dgi.immatriculation.service.impl;

import lombok.RequiredArgsConstructor;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tn.gov.dgi.immatriculation.dto.response.CinOcrResponseDTO;
import tn.gov.dgi.immatriculation.exception.OcrExtractionException;
import tn.gov.dgi.immatriculation.model.ResultatOcr;
import tn.gov.dgi.immatriculation.model.TypePieceJointe;
import tn.gov.dgi.immatriculation.service.OcrService;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Service OCR — BV7 Fixed.
 *
 * PROBLÈME RACINE : Tesseract recevait une image 2x36 px car :
 *   1. Le JPEG smartphone contient une miniature EXIF (thumbnail) de ~2x36 px
 *   2. ImageIO.read() peut parfois lire la miniature au lieu de l'image principale
 *   3. Le tag EXIF Orientation était ignoré → image pivotée de 90° non corrigée
 *
 * SOLUTION :
 *   - Utiliser ImageIO avec sélection explicite du reader et setInput(stream, false)
 *     pour lire l'IMAGE PRINCIPALE et non la miniature EXIF
 *   - Lire le tag EXIF Orientation depuis les bytes bruts et corriger la rotation
 *   - Upscale basé sur le côté le plus long (portrait OU paysage)
 */
@Service
@RequiredArgsConstructor
public class OcrServiceImpl implements OcrService {

    private static final Logger log = LoggerFactory.getLogger(OcrServiceImpl.class);

    private static final long MAX_SIZE_BYTES = 15 * 1024 * 1024L;
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "application/pdf");
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "pdf");

    /** Côté minimum pour une bonne précision Tesseract sur texte arabe. */
    private static final int MIN_DIMENSION = 2000;

    @Value("${app.ocr.tessdata-path}")
    private String tessdataPath;

    @Value("${app.ocr.langue:ara+fra}")
    private String langueDefaut;

    // ── OcrService interface ──────────────────────────────────────────────────

    @Override
    public ResultatOcr extraireDonnees(MultipartFile fichier, TypePieceJointe typePiece) {
        CinOcrResponseDTO dto = extraire(fichier, langueDefaut, typePiece);
        return ResultatOcr.builder()
                .nomDetecte(dto.getNomDetecte())
                .prenomDetecte(dto.getPrenomDetecte())
                .numeroPieceDetecte(dto.getNumeroPieceDetecte())
                .dateNaissanceDetectee(dto.getDateNaissanceDetectee())
                .texteBrutExtrait(dto.getTexte())
                .scoreConfiance(dto.getConfiance())
                .extractionReussie(dto.getNumeroPieceDetecte() != null)
                .dateExtraction(LocalDateTime.now())
                .messageErreur(dto.getNumeroPieceDetecte() == null
                        ? "Numéro CIN non détecté" : null)
                .build();
    }

    public CinOcrResponseDTO extraire(MultipartFile fichier, String languages,
                                      TypePieceJointe typePiece) {
        validerFichier(fichier);

        String texteBrut;
        try {
            byte[] bytes = fichier.getBytes();
            List<BufferedImage> pages = chargerImages(bytes, fichier.getContentType());
            texteBrut = ocrPages(pages, languages != null ? languages : langueDefaut);
        } catch (IOException e) {
            throw new OcrExtractionException("Impossible de lire le fichier : " + e.getMessage());
        } catch (TesseractException e) {
            throw new OcrExtractionException("Erreur moteur OCR : " + e.getMessage());
        }

        CinArabicParser.ParseResult parsed = CinArabicParser.parse(texteBrut);

        return CinOcrResponseDTO.builder()
                .nomDetecte(parsed.nom)
                .prenomDetecte(parsed.prenom)
                .numeroPieceDetecte(parsed.numeroCin)
                .dateNaissanceDetectee(parsed.dateNaissance)
                .lieuNaissance(parsed.lieuNaissance)
                .texte(texteBrut)
                .confiance(parsed.confiance)
                .build();
    }

    // ── Validation ────────────────────────────────────────────────────────────

    private void validerFichier(MultipartFile fichier) {
        if (fichier == null || fichier.isEmpty()) {
            throw new OcrExtractionException("Aucun fichier fourni.");
        }
        if (fichier.getSize() > MAX_SIZE_BYTES) {
            throw new OcrExtractionException(
                    "Fichier trop volumineux : " + (fichier.getSize() / 1024 / 1024) + " Mo.");
        }
        String ct = fichier.getContentType();
        if (ct == null || !ALLOWED_TYPES.contains(ct.toLowerCase())) {
            throw new OcrExtractionException("Type non autorisé : " + ct);
        }
        String name = fichier.getOriginalFilename();
        if (name != null) {
            String ext = name.contains(".")
                    ? name.substring(name.lastIndexOf('.') + 1).toLowerCase() : "";
            if (!ALLOWED_EXTENSIONS.contains(ext)) {
                throw new OcrExtractionException("Extension non autorisée : " + ext);
            }
        }
    }

    // ── Chargement images ─────────────────────────────────────────────────────

    private List<BufferedImage> chargerImages(byte[] bytes, String contentType)
            throws IOException {
        List<BufferedImage> pages = new ArrayList<>();

        if ("application/pdf".equals(contentType)) {
            try (PDDocument pdf = Loader.loadPDF(bytes)) {
                PDFRenderer renderer = new PDFRenderer(pdf);
                for (int i = 0; i < pdf.getNumberOfPages(); i++) {
                    BufferedImage img = renderer.renderImageWithDPI(i, 300, ImageType.RGB);
                    pages.add(pretraiter(img));
                }
            }
        } else {
            // ── FIX CRITIQUE : lire l'image principale, PAS la miniature EXIF ──
            //
            // ImageIO.read(InputStream) peut lire la miniature EXIF intégrée
            // dans le JPEG (souvent 2x36 px) au lieu de l'image principale.
            //
            // Solution : utiliser ImageReader avec setInput(stream, false, true)
            // Le 3ème paramètre ignoreMetadata=false permet de choisir l'index
            // de l'image. index=0 = image principale (jamais la miniature).
            BufferedImage original = lireImagePrincipale(bytes);

            if (original == null) {
                throw new IOException("Format d'image non reconnu ou fichier corrompu.");
            }

            log.info("Image originale lue : {}x{} px", original.getWidth(), original.getHeight());

            // Corriger la rotation EXIF
            int orientation = lireOrientationExif(bytes);
            log.info("Orientation EXIF : {}", orientation);
            BufferedImage corrige = appliquerRotation(original, orientation);

            pages.add(pretraiter(corrige));
        }

        if (pages.isEmpty()) throw new IOException("Aucune page lisible.");
        return pages;
    }

    /**
     * Lit l'IMAGE PRINCIPALE du JPEG en utilisant ImageReader directement.
     * Évite la lecture de la miniature EXIF qu'ImageIO.read() peut retourner.
     */
    private BufferedImage lireImagePrincipale(byte[] bytes) throws IOException {
        try (ImageInputStream iis = ImageIO.createImageInputStream(
                new ByteArrayInputStream(bytes))) {

            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (!readers.hasNext()) {
                // Fallback : méthode standard
                return ImageIO.read(new ByteArrayInputStream(bytes));
            }

            ImageReader reader = readers.next();
            try {
                reader.setInput(iis, false, false); // seekForwardOnly=false, ignoreMetadata=false

                // index 0 = image principale (la miniature EXIF est à un index différent)
                int numImages = reader.getNumImages(true);
                log.info("JPEG contient {} image(s) (index 0 = principale)", numImages);

                // Toujours lire l'image avec la plus grande résolution
                BufferedImage best = null;
                int bestPixels = 0;
                for (int i = 0; i < numImages; i++) {
                    try {
                        int w = reader.getWidth(i);
                        int h = reader.getHeight(i);
                        int pixels = w * h;
                        log.info("  Image[{}] : {}x{} px", i, w, h);
                        if (pixels > bestPixels) {
                            bestPixels = pixels;
                            best = reader.read(i);
                        }
                    } catch (Exception e) {
                        log.debug("Image[{}] illisible : {}", i, e.getMessage());
                    }
                }
                return best;
            } finally {
                reader.dispose();
            }
        }
    }

    // ── Correction EXIF ───────────────────────────────────────────────────────

    private BufferedImage appliquerRotation(BufferedImage image, int orientation) {
        return switch (orientation) {
            case 3 -> pivoter(image, 180);
            case 6 -> pivoter(image, 90);
            case 8 -> pivoter(image, 270);
            default -> image;
        };
    }

    private int lireOrientationExif(byte[] bytes) {
        for (int i = 0; i < bytes.length - 4; i++) {
            if ((bytes[i] & 0xFF) == 0xFF && (bytes[i + 1] & 0xFF) == 0xE1) {
                for (int j = i + 4; j < Math.min(i + 200, bytes.length - 6); j++) {
                    if (bytes[j] == 'E' && bytes[j+1] == 'x' && bytes[j+2] == 'i' && bytes[j+3] == 'f') {
                        int tiffStart = j + 6;
                        if (tiffStart + 8 >= bytes.length) break;
                        boolean le = bytes[tiffStart] == 'I';
                        int ifdOffset = tiffStart + readShort(bytes, tiffStart + 4, le);
                        if (ifdOffset + 2 >= bytes.length) break;
                        int n = readShort(bytes, ifdOffset, le);
                        for (int e = 0; e < n && e < 50; e++) {
                            int off = ifdOffset + 2 + e * 12;
                            if (off + 12 >= bytes.length) break;
                            if (readShort(bytes, off, le) == 0x0112) {
                                return readShort(bytes, off + 8, le);
                            }
                        }
                        break;
                    }
                }
                break;
            }
        }
        return 1;
    }

    private int readShort(byte[] b, int offset, boolean le) {
        if (offset + 1 >= b.length) return 0;
        int lo = b[offset] & 0xFF, hi = b[offset + 1] & 0xFF;
        return le ? (hi << 8) | lo : (lo << 8) | hi;
    }

    private BufferedImage pivoter(BufferedImage src, int deg) {
        int w = src.getWidth(), h = src.getHeight();
        int nw = (deg == 90 || deg == 270) ? h : w;
        int nh = (deg == 90 || deg == 270) ? w : h;
        BufferedImage dest = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = dest.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.translate(nw / 2.0, nh / 2.0);
        g.rotate(Math.toRadians(deg));
        g.translate(-w / 2.0, -h / 2.0);
        g.drawImage(src, 0, 0, null);
        g.dispose();
        log.info("Image pivotée {}° : {}x{} → {}x{}", deg, w, h, nw, nh);
        return dest;
    }

    // ── Prétraitement ─────────────────────────────────────────────────────────

    private BufferedImage pretraiter(BufferedImage src) {
        int w = src.getWidth(), h = src.getHeight();
        int maxDim = Math.max(w, h);

        if (maxDim < MIN_DIMENSION) {
            double scale = (double) MIN_DIMENSION / maxDim;
            int nw = (int)(w * scale), nh = (int)(h * scale);
            BufferedImage up = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = up.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.drawImage(src, 0, 0, nw, nh, null);
            g.dispose();
            log.info("Upscale : {}x{} → {}x{}", w, h, nw, nh);
            src = up;
        }

        // Niveaux de gris → meilleure précision arabe
        BufferedImage gray = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2 = gray.createGraphics();
        g2.drawImage(src, 0, 0, null);
        g2.dispose();

        log.info("Image finale OCR : {}x{} px (grayscale)", gray.getWidth(), gray.getHeight());
        return gray;
    }

    // ── OCR ──────────────────────────────────────────────────────────────────

    private String ocrPages(List<BufferedImage> pages, String languages) throws TesseractException {
        Tesseract tess = new Tesseract();
        tess.setDatapath(tessdataPath);
        tess.setLanguage(languages);
        tess.setPageSegMode(3);
        tess.setOcrEngineMode(1);
        tess.setVariable("preserve_interword_spaces", "1");

        StringBuilder sb = new StringBuilder();
        for (BufferedImage page : pages) {
            if (sb.length() > 0) sb.append("\n--- PAGE ---\n");
            String t = tess.doOCR(page);
            log.info("OCR résultat ({} chars) :\n{}", t.length(), t);
            sb.append(t);
        }
        return sb.toString().trim();
    }
}