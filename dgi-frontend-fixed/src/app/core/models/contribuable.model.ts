export type TypeContribuable = 'PERSONNE_PHYSIQUE' | 'PERSONNE_MORALE';

export interface Contribuable {
  id?: string;
  typeContribuable: TypeContribuable;

  // Personne physique
  nom?: string;
  prenom?: string;
  cin?: string;
  numeroPasseport?: string;

  // Personne morale
  raisonSociale?: string;
  registreCommerce?: string;

  // Communs
  adresse: string;
  ville: string;
  codePostal: string;
  telephone: string;
  email: string;
  dateNaissanceOuCreation: string; // format ISO (YYYY-MM-DD)
}