/**
 * Statuts possibles d'un dossier d'immatriculation fiscale.
 * Doit correspondre EXACTEMENT à l'enum StatutDossier côté backend.
 * Transitions : BROUILLON -> SOUMIS -> EN_TRAITEMENT -> VALIDE | REJETE
 *               REJETE -> BROUILLON
 */
export enum StatutDossier {
  BROUILLON     = 'BROUILLON',
  SOUMIS        = 'SOUMIS',
  EN_TRAITEMENT = 'EN_TRAITEMENT',   // FIX 1: était EN_COURS_TRAITEMENT, le backend utilise EN_TRAITEMENT
  VALIDE        = 'VALIDE',
  REJETE        = 'REJETE',
}
