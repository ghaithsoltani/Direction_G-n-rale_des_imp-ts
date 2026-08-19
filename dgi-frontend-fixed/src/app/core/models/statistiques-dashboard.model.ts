/**
 * Correspond à StatistiquesDashboardDTO côté backend.
 * Renvoyé par GET /api/dossiers/statistiques.
 */
export interface StatistiquesDashboard {
  totalDossiers: number;
  brouillons: number;
  soumis: number;
  enTraitement: number;
  valides: number;
  rejetes: number;
  /** 12 valeurs (index 0 = Janvier … 11 = Décembre) */
  evolutionMensuelle: number[];
}
