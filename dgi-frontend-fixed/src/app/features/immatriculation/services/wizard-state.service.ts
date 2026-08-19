import { Injectable, signal } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { debounceTime } from 'rxjs';
import { ResultatVerificationFaciale } from '../../../core/models/resultat-verification-faciale.model';
import { ChampsExtraitsOcr, PieceJointe, TypePieceJointe } from '../../../core/models/piece-jointe.model';

@Injectable({ providedIn: 'root' })
export class WizardStateService {
  private readonly brouillonKey = 'dgi-immatriculation-brouillon';
  readonly etapeCourante = signal<number>(1);
  readonly totalEtapes = 5;
  readonly resultatFacial = signal<ResultatVerificationFaciale | null>(null);
  readonly piecesJointes = signal<PieceJointe[]>([]);
  readonly photoCapturee = signal<string | null>(null);
  readonly contribuableId = signal<string | null>(null);
  readonly dossierId = signal<string | null>(null);
  readonly brouillonRestaure = signal(false);
  readonly derniereSauvegarde = signal<string | null>(null);
  readonly formulaire: FormGroup;

  constructor(private fb: FormBuilder) {
    this.formulaire = this.construireFormulaire();
    this.restaurerBrouillon();
    this.formulaire.valueChanges.pipe(debounceTime(500)).subscribe(() => this.enregistrerBrouillon());
  }

  etapeSuivante(): void {
    if (this.etapeCourante() < this.totalEtapes)
      this.etapeCourante.update(e => e + 1);
    this.enregistrerBrouillon();
  }

  etapePrecedente(): void {
    if (this.etapeCourante() > 1)
      this.etapeCourante.update(e => e - 1);
    this.enregistrerBrouillon();
  }

  allerEtape(n: number): void {
    if (n >= 1 && n <= this.totalEtapes) {
      this.etapeCourante.set(n);
      this.enregistrerBrouillon();
    }
  }

  reinitialiser(): void {
    this.etapeCourante.set(1);
    this.formulaire.reset();
    this.resultatFacial.set(null);
    this.piecesJointes.set([]);
    this.photoCapturee.set(null);
    this.contribuableId.set(null);
    this.dossierId.set(null);
    this.brouillonRestaure.set(false);
    this.derniereSauvegarde.set(null);
    this.supprimerBrouillon();
  }

  preRemplirDepuisOcr(champs: ChampsExtraitsOcr, typePiece: TypePieceJointe = 'CIN'): void {
    const g = this.formulaire.get('informationsGenerales');
    if (!g) return;
    if (champs.nom) g.get('nom')?.setValue(champs.nom);
    if (champs.prenom) g.get('prenom')?.setValue(champs.prenom);
    if (champs.cin) g.get(typePiece === 'PASSEPORT' ? 'numeroPasseport' : 'cin')?.setValue(champs.cin);
    if (champs.dateNaissance) g.get('dateNaissanceOuCreation')?.setValue(champs.dateNaissance);
    if (champs.adresse) g.get('adresse')?.setValue(champs.adresse);
  }

  private enregistrerBrouillon(): void {
    if (typeof localStorage === 'undefined') return;
    const maintenant = new Date().toISOString();
    localStorage.setItem(this.brouillonKey, JSON.stringify({
      formulaire: this.formulaire.getRawValue(),
      etape: this.etapeCourante(),
      sauvegardeLe: maintenant,
    }));
    this.derniereSauvegarde.set(maintenant);
  }

  private restaurerBrouillon(): void {
    if (typeof localStorage === 'undefined') return;
    try {
      const brouillon = JSON.parse(localStorage.getItem(this.brouillonKey) ?? 'null') as {
        formulaire?: object; etape?: number; sauvegardeLe?: string;
      } | null;
      if (!brouillon?.formulaire) return;
      this.formulaire.patchValue(brouillon.formulaire);
      if (brouillon.etape && brouillon.etape >= 1 && brouillon.etape <= this.totalEtapes) this.etapeCourante.set(brouillon.etape);
      this.derniereSauvegarde.set(brouillon.sauvegardeLe ?? null);
      this.brouillonRestaure.set(true);
    } catch {
      this.supprimerBrouillon();
    }
  }

  private supprimerBrouillon(): void {
    if (typeof localStorage !== 'undefined') localStorage.removeItem(this.brouillonKey);
  }

  private construireFormulaire(): FormGroup {
    return this.fb.group({
      informationsGenerales: this.fb.group({
        typeContribuable: ['PERSONNE_PHYSIQUE', Validators.required],
        nom: [''],
        prenom: [''],
        cin: [''],
        numeroPasseport: [''],
        raisonSociale: [''],
        registreCommerce: [''],
        adresse: ['', Validators.required],
        ville: ['', Validators.required],
        codePostal: ['', [Validators.required, Validators.pattern(/^\d{4}$/)]],
        telephone: ['', [Validators.required, Validators.pattern(/^[2-9]\d{7}$/)]],
        email: ['', [Validators.required, Validators.email]],
        dateNaissanceOuCreation: ['', Validators.required],
      }),
      activite: this.fb.group({
        secteurActivite: ['', Validators.required],
        codeActivitePrincipale: ['', Validators.required],
        libelleActivite: ['', Validators.required],
        regimeFiscal: ['REEL', Validators.required],
        adresseExercice: ['', Validators.required],
        villeExercice: ['', Validators.required],
        dateDebutActivite: ['', Validators.required],
      }),
    });
  }
}
