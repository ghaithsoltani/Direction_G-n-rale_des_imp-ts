export type TypePieceJointe = 'CIN' | 'PASSEPORT' | 'JUSTIFICATIF_DOMICILE' | 'REGISTRE_COMMERCE' | 'AUTRE';

export interface PieceJointe {
  id?: string;
  type: TypePieceJointe;
  nomFichier: string;
  url?: string;          // URL de consultation une fois uploadée
  tailleOctets?: number;
  dateUpload?: string;
  fichier?: File;        // fichier source conservé pour l’upload multipart
  ocr?: ResultatOcrPiece;
}

/** Champs extraits automatiquement par l'OCR à partir d'une pièce jointe */
export interface ChampsExtraitsOcr {
  nom?: string;
  prenom?: string;
  cin?: string;
  dateNaissance?: string;
  adresse?: string;
}

export interface ResultatOcrPiece {
  statut: 'EN_COURS' | 'TERMINE' | 'ERREUR';
  champs?: ChampsExtraitsOcr;
  erreur?: string;
}
