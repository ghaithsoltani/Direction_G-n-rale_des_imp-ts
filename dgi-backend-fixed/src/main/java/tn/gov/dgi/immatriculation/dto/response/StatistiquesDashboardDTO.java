package tn.gov.dgi.immatriculation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO renvoyé par GET /api/dossiers/statistiques.
 * Contient les compteurs par statut et l'évolution mensuelle
 * pour alimenter les graphiques du dashboard agent DGI.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatistiquesDashboardDTO {

    private long totalDossiers;
    private long brouillons;
    private long soumis;
    private long enTraitement;
    private long valides;
    private long rejetes;

    /** Évolution mensuelle : 12 valeurs (indice 0 = Janvier, 11 = Décembre) */
    private List<Long> evolutionMensuelle;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PointMensuel {
        private int mois;   // 1-12
        private long total;
    }
}
