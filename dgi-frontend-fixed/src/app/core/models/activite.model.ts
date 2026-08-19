export type RegimeFiscal = 'REEL' | 'FORFAITAIRE' | 'SIMPLIFIE';

export interface Activite {
  secteurActivite: string;
  codeActivitePrincipale: string;
  libelleActivite: string;
  regimeFiscal: RegimeFiscal;
  adresseExercice: string;
  villeExercice: string;
  dateDebutActivite: string; // ISO date
}