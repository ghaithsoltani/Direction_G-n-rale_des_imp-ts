export interface ResultatVerificationFaciale {
  scoreSimilarite: number;   // ex. 0.0 à 1.0
  verifie: boolean;          // true si le score dépasse le seuil de validation
  message?: string;
}