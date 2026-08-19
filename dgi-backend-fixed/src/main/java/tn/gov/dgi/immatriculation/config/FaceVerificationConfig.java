package tn.gov.dgi.immatriculation.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Paths;

@Configuration
public class FaceVerificationConfig {

    private static final Logger log = LoggerFactory.getLogger(FaceVerificationConfig.class);

    @Value("${app.face-verification.cascade-path}")
    private String cascadePath;

    @Value("${app.face-verification.seuil-acceptation}")
    private Double seuilAcceptation;

    @PostConstruct
    public void verifierConfiguration() {
        if (!Files.exists(Paths.get(cascadePath))) {
            log.warn("ATTENTION : le fichier de cascade Haar ({}) est introuvable. "
                    + "Le service de vérification faciale échouera à l'exécution. "
                    + "Vérifiez app.face-verification.cascade-path.", cascadePath);
        } else {
            log.info("Configuration de vérification faciale validée (seuil={})", seuilAcceptation);
        }

        if (seuilAcceptation < 0.5 || seuilAcceptation > 0.95) {
            log.warn("Le seuil d'acceptation configuré ({}) est en dehors de la plage recommandée [0.5, 0.95] "
                    + "— risque de trop de faux positifs ou faux négatifs.", seuilAcceptation);
        }
    }
}