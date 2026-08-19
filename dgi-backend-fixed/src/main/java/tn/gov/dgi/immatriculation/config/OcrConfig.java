package tn.gov.dgi.immatriculation.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class OcrConfig {

    private static final Logger log = LoggerFactory.getLogger(OcrConfig.class);

    @Value("${app.ocr.tessdata-path}")
    private String tessdataPath;

    /**
     * Vérifie au démarrage que le dossier tessdata existe et contient au
     * moins un fichier .traineddata — évite de découvrir le problème
     * seulement au premier appel d'un contribuable en train de remplir son
     * dossier.
     */
    @PostConstruct
    public void verifierTessdata() {
        Path chemin = Paths.get(tessdataPath);
        if (!Files.isDirectory(chemin)) {
            log.warn("ATTENTION : le répertoire tessdata configuré ({}) est introuvable. "
                    + "Le service OCR échouera à l'exécution. Vérifiez app.ocr.tessdata-path.", tessdataPath);
            return;
        }
        boolean contientTraineddata;
        try (var stream = Files.list(chemin)) {
            contientTraineddata = stream.anyMatch(p -> p.toString().endsWith(".traineddata"));
        } catch (Exception e) {
            log.warn("Impossible de vérifier le contenu du répertoire tessdata : {}", e.getMessage());
            return;
        }
        if (!contientTraineddata) {
            log.warn("ATTENTION : aucun fichier .traineddata trouvé dans {}", tessdataPath);
        } else {
            log.info("Configuration OCR validée : tessdata trouvé dans {}", tessdataPath);
        }
    }
}