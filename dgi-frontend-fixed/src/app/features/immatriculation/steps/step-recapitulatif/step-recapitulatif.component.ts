import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { forkJoin, map } from 'rxjs';
import { WizardStateService } from '../../services/wizard-state.service';
import { ToastService } from '../../../../core/services/toast.service';
import { ContribuableService } from '../../../../core/services/contribuable.service';
import { DossierService } from '../../../../core/services/dossier.service';
import { DocumentUploadService } from '../../../../core/services/document-upload.service';
import { FaceVerificationService } from '../../../../core/services/face-verification.service';
import { PieceJointe } from '../../../../core/models/piece-jointe.model';
import { IconComponent } from '../../../../shared/components/icon/icon.component';

@Component({
  selector: 'app-step-recapitulatif',
  standalone: true,
  imports: [CommonModule, IconComponent],
  template: `
    <div class="space-y-6">
      <div class="rounded-2xl border border-blue-100 bg-blue-50/70 p-4">
        <h2 class="text-2xl font-semibold text-slate-900">Récapitulatif du dossier</h2>
        <p class="mt-2 text-sm text-slate-600">Vérifiez les informations avant la soumission finale.</p>
      </div>

      <div class="space-y-4">
        <div class="rounded-[24px] border border-slate-200 bg-slate-50 p-6">
          <div class="mb-4 flex items-center justify-between">
            <h3 class="flex items-center gap-2 text-lg font-semibold text-slate-900"><app-icon name="user" size="sm" />Informations générales</h3>
            <button (click)="wizardState.allerEtape(2)" class="text-sm font-semibold text-blue-700">Modifier</button>
          </div>
          <div class="grid gap-3 text-sm sm:grid-cols-2">
            @for (champ of champsInfosGenerales(); track champ.label) {
              @if (champ.valeur) {
                <div class="rounded-2xl border border-slate-200 bg-white p-3">
                  <span class="block text-xs uppercase tracking-[0.2em] text-slate-500">{{ champ.label }}</span>
                  <span class="mt-1 block font-semibold text-slate-800">{{ champ.valeur }}</span>
                </div>
              }
            }
          </div>
        </div>

        <div class="rounded-[24px] border border-slate-200 bg-slate-50 p-6">
          <div class="mb-4 flex items-center justify-between">
            <h3 class="flex items-center gap-2 text-lg font-semibold text-slate-900"><app-icon name="briefcase" size="sm" />Activité professionnelle</h3>
            <button (click)="wizardState.allerEtape(3)" class="text-sm font-semibold text-blue-700">Modifier</button>
          </div>
          <div class="grid gap-3 text-sm sm:grid-cols-2">
            @for (champ of champsActivite(); track champ.label) {
              @if (champ.valeur) {
                <div class="rounded-2xl border border-slate-200 bg-white p-3">
                  <span class="block text-xs uppercase tracking-[0.2em] text-slate-500">{{ champ.label }}</span>
                  <span class="mt-1 block font-semibold text-slate-800">{{ champ.valeur }}</span>
                </div>
              }
            }
          </div>
        </div>

        <div class="rounded-[24px] border border-slate-200 bg-slate-50 p-6">
          <div class="mb-4 flex items-center justify-between">
            <h3 class="flex items-center gap-2 text-lg font-semibold text-slate-900"><app-icon name="documents" size="sm" />Documents</h3>
            <button (click)="wizardState.allerEtape(1)" class="text-sm font-semibold text-blue-700">Modifier</button>
          </div>
          @if (wizardState.piecesJointes().length > 0) {
            <div class="space-y-2">
              @for (piece of wizardState.piecesJointes(); track piece.nomFichier) {
                <div class="flex items-center gap-2 rounded-2xl border border-slate-200 bg-white px-3 py-2 text-sm text-slate-700">
                  <span class="text-emerald-500"><app-icon name="success" size="sm" /></span>
                  <span>{{ piece.nomFichier }}</span>
                  <span class="text-slate-400">({{ piece.type }})</span>
                </div>
              }
            </div>
          } @else {
            <p class="text-sm text-slate-500">Aucun document</p>
          }
        </div>

        <div class="rounded-[24px] border border-slate-200 bg-slate-50 p-6">
          <div class="mb-4 flex items-center justify-between">
            <h3 class="flex items-center gap-2 text-lg font-semibold text-slate-900"><app-icon name="id-card" size="sm" />Vérification faciale</h3>
            <button (click)="wizardState.allerEtape(4)" class="text-sm font-semibold text-blue-700">Modifier</button>
          </div>
          @if (wizardState.resultatFacial()) {
            <div class="flex items-center gap-3 rounded-2xl border border-emerald-200 bg-emerald-50 p-4">
              <span [class.text-emerald-600]="wizardState.resultatFacial()!.verifie" [class.text-red-600]="!wizardState.resultatFacial()!.verifie"><app-icon [name]="wizardState.resultatFacial()!.verifie ? 'success' : 'danger'" size="lg" /></span>
              <div>
                <p class="text-sm font-semibold"
                   [class.text-emerald-700]="wizardState.resultatFacial()!.verifie"
                   [class.text-red-700]="!wizardState.resultatFacial()!.verifie">
                  {{ wizardState.resultatFacial()!.verifie ? 'Identité vérifiée' : 'Non vérifiée' }}
                </p>
                <p class="text-xs text-slate-500">Score : {{ (wizardState.resultatFacial()!.scoreSimilarite * 100).toFixed(0) }}%</p>
              </div>
            </div>
          } @else {
            <p class="text-sm text-slate-500">Vérification non effectuée</p>
          }
        </div>
      </div>

      <div class="flex flex-wrap justify-between gap-3 border-t border-slate-200 pt-6">
        <button (click)="wizardState.etapePrecedente()" class="rounded-2xl bg-slate-100 px-6 py-3 text-sm font-semibold text-slate-700 transition hover:bg-slate-200">← Précédent</button>
        <button (click)="soumettreDossier()" [disabled]="soumissionEnCours()" class="flex items-center gap-2 rounded-2xl bg-emerald-600 px-8 py-3 text-sm font-semibold text-white transition hover:bg-emerald-700 disabled:cursor-not-allowed disabled:opacity-60">
          @if (soumissionEnCours()) {
            <span class="h-4 w-4 animate-spin rounded-full border-2 border-white border-t-transparent"></span>
            Soumission...
          } @else {
            <app-icon name="success" size="sm" /> Soumettre le dossier
          }
        </button>
      </div>
    </div>
  `,
})
export class StepRecapitulatifComponent {
  readonly wizardState = inject(WizardStateService);
  private readonly contribuableService = inject(ContribuableService);
  private readonly dossierService = inject(DossierService);
  private readonly documentUploadService = inject(DocumentUploadService);
  private readonly faceVerificationService = inject(FaceVerificationService);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);

  readonly soumissionEnCours = signal(false);

  champsInfosGenerales() {
    const v = this.wizardState.formulaire.get('informationsGenerales')?.value;
    if (!v) return [];
    return [
      { label: 'Type', valeur: v.typeContribuable === 'PERSONNE_PHYSIQUE' ? 'Personne physique' : 'Personne morale' },
      { label: 'Nom', valeur: v.nom },
      { label: 'Prénom', valeur: v.prenom },
      { label: 'Raison sociale', valeur: v.raisonSociale },
      { label: 'CIN', valeur: v.cin },
      { label: 'Passeport', valeur: v.numeroPasseport },
      { label: 'Adresse', valeur: v.adresse },
      { label: 'Ville', valeur: v.ville },
      { label: 'Code postal', valeur: v.codePostal },
      { label: 'Téléphone', valeur: v.telephone },
      { label: 'Email', valeur: v.email },
      { label: 'Date naissance/création', valeur: v.dateNaissanceOuCreation },
    ];
  }

  champsActivite() {
    const v = this.wizardState.formulaire.get('activite')?.value;
    if (!v) return [];
    return [
      { label: 'Secteur', valeur: v.secteurActivite },
      { label: 'Code activité', valeur: v.codeActivitePrincipale },
      { label: 'Libellé', valeur: v.libelleActivite },
      { label: 'Régime fiscal', valeur: v.regimeFiscal },
      { label: 'Adresse exercice', valeur: v.adresseExercice },
      { label: 'Ville exercice', valeur: v.villeExercice },
      { label: 'Début activité', valeur: v.dateDebutActivite },
    ];
  }

  soumettreDossier(): void {
    const infosGenerales = this.wizardState.formulaire.get('informationsGenerales')?.value;
    const activite = this.wizardState.formulaire.get('activite')?.value;

    if (!infosGenerales || !activite || this.wizardState.formulaire.invalid) {
      this.wizardState.formulaire.markAllAsTouched();
      this.toast.afficherErreur('Veuillez compléter toutes les étapes du wizard avant la soumission.');
      return;
    }

    const estPersonnePhysique = infosGenerales.typeContribuable === 'PERSONNE_PHYSIQUE';
    const pieces = this.wizardState.piecesJointes();
    const pieceIdentite = pieces.find((piece) => piece.type === 'CIN' || piece.type === 'PASSEPORT');
    if (!pieces.length || (estPersonnePhysique && !pieceIdentite)) {
      this.toast.afficherErreur('Ajoutez les documents requis avant de soumettre le dossier.');
      return;
    }
    if (estPersonnePhysique && !this.wizardState.photoCapturee()) {
      this.toast.afficherErreur('Prenez une photo pour terminer la vérification faciale.');
      return;
    }

    this.soumissionEnCours.set(true);

    // Étape 1 : créer le contribuable
    this.contribuableService.creer(infosGenerales, activite).subscribe({
      next: (contribuableCree) => {
        this.wizardState.contribuableId.set(contribuableCree.id ?? null);

        // Étape 2 : créer le dossier (statut BROUILLON)
        this.dossierService.creer({ contribuableId: contribuableCree.id ?? '' }).subscribe({
          next: (dossierCree) => {
            this.wizardState.dossierId.set(dossierCree.id ?? null);
            const dossierId = dossierCree.id ?? '';

            const uploads$ = pieces
              .filter((p) => !!p.fichier)
              .map((p) => this.documentUploadService.uploaderPieceSimple(dossierId, p.type, p.fichier!).pipe(
                map((reponse) => ({ pieceLocale: p, pieceUploadée: reponse as PieceJointe }))
              ));

            const finaliser = () => {
              // FIX 11: appel manquant à soumettre() — sans cet appel le dossier
              // restait en BROUILLON et n'était jamais visible par les agents DGI.
              this.dossierService.soumettre(dossierId).subscribe({
                next: () => {
                  this.soumissionEnCours.set(false);
                  this.toast.afficherSucces('Dossier soumis avec succès ! Vous recevrez une confirmation par email.');
                  this.wizardState.reinitialiser();
                  // FIX 10: redirection vers /dashboard, pas /auth/login
                  this.router.navigate(['/dashboard']);
                },
                error: () => {
                  this.soumissionEnCours.set(false);
                  this.toast.afficherErreur('Dossier créé mais la soumission finale a échoué. Contactez le support.');
                },
              });
            };

            const verifierPuisFinaliser = (piecesUploadées: PieceJointe[]) => {
              if (!estPersonnePhysique) {
                finaliser();
                return;
              }

              const reference = piecesUploadées.find((piece) => piece.type === 'CIN' || piece.type === 'PASSEPORT');
              const photoLive = this.photoVersBlob(this.wizardState.photoCapturee()!);
              if (!reference?.id) {
                this.soumissionEnCours.set(false);
                this.toast.afficherErreur('La pièce d’identité téléversée ne peut pas être utilisée pour la vérification faciale.');
                return;
              }

              this.faceVerificationService.verifier(dossierId, reference.id, photoLive).subscribe({
                next: (resultat) => {
                  this.wizardState.resultatFacial.set(resultat);
                  finaliser();
                },
                error: () => {
                  this.soumissionEnCours.set(false);
                  this.toast.afficherErreur('La vérification faciale a échoué. Reprenez votre photo et réessayez.');
                },
              });
            };

            if (uploads$.length === 0) {
              verifierPuisFinaliser([]);
              return;
            }

            forkJoin(uploads$).subscribe({
              next: (uploads) => {
                const piecesUploadées = uploads.map(({ pieceLocale, pieceUploadée }) => ({
                  ...pieceLocale,
                  ...pieceUploadée,
                  fichier: pieceLocale.fichier,
                  ocr: pieceLocale.ocr,
                }));
                this.wizardState.piecesJointes.set(piecesUploadées);
                verifierPuisFinaliser(piecesUploadées);
              },
              error: () => {
                this.soumissionEnCours.set(false);
                this.toast.afficherErreur('Erreur pendant le téléversement des pièces jointes.');
              },
            });
          },
          error: () => {
            this.soumissionEnCours.set(false);
            this.toast.afficherErreur('Erreur lors de la création du dossier. Réessayez.');
          },
        });
      },
      error: () => {
        this.soumissionEnCours.set(false);
        this.toast.afficherErreur('Erreur lors de la création du contribuable. Réessayez.');
      },
    });
  }

  private photoVersBlob(photo: string): Blob {
    const [, contenu] = photo.split(',', 2);
    const octets = atob(contenu ?? '');
    const donnees = new Uint8Array(octets.length);
    for (let index = 0; index < octets.length; index++) donnees[index] = octets.charCodeAt(index);
    return new Blob([donnees], { type: 'image/jpeg' });
  }
}
