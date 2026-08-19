import { PieceJointe } from './piece-jointe.model';
import { ResultatVerificationFaciale } from './resultat-verification-faciale.model';
import { StatutDossier } from './statut-dossier.enum';

/**
 * Résumé du contribuable tel que renvoyé par DossierResponseDTO.ContribuableResumeDTO.
 * Champ 'nomAffichage' = prénom+nom (PP) ou raison sociale (PM) calculé côté backend.
 */
export interface ContribuableResume {
  id?: string;
  nomAffichage?: string;   // prénom + nom OU raison sociale (calculé côté mapper)
  cin?: string;
  email?: string;
  // Champs complémentaires présents sur ContribuableResponseDTO complet (GET /contribuables/:id)
  nom?: string;
  prenom?: string;
  raisonSociale?: string;
  typeContribuable?: 'PERSONNE_PHYSIQUE' | 'PERSONNE_MORALE';
  telephone?: string;
}

export interface DossierImmatriculation {
  id?: string;
  numeroDossier?: string;
  statut: StatutDossier;
  contribuable: ContribuableResume;
  piecesJointes: PieceJointe[];
  resultatVerificationFaciale?: ResultatVerificationFaciale;
  commentaireAgent?: string;
  agentTraitantId?: string;
  dateCreation?: string;
  dateDerniereModification?: string;
  dateSoumission?: string;
}

export interface FiltresDossier {
  statut?: StatutDossier;
  recherche?: string;
  page: number;
  taille: number;
}

export interface PageResultat<T> {
  content?: T[];
  contenu?: T[];
  totalElements?: number;
  totalElementsCount?: number;
  totalPages?: number;
  pageActuelle?: number;
  number?: number;
  size?: number;
}
