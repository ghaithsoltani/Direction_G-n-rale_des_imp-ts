import { Component, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { WizardStateService } from '../../services/wizard-state.service';

@Component({
  selector: 'app-step-infos-generales',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="space-y-6">

      <!-- ── En-tête ── -->
      <div class="rounded-2xl border border-blue-100 bg-blue-50/70 p-4">
        <h2 class="text-2xl font-semibold text-slate-900">Informations générales</h2>
        <p class="mt-2 text-sm text-slate-600">
          Renseignez vos informations personnelles ou celles de votre entreprise avec précision.
        </p>
      </div>

      <!-- ── Bandeau OCR pré-rempli ── -->
      @if (aDesChampsPreremplis()) {
        <div class="flex items-start gap-3 rounded-2xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-800">
          <svg class="mt-0.5 h-5 w-5 flex-shrink-0 text-emerald-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
          </svg>
          <span>
            <strong>Formulaire prérempli automatiquement</strong> — Les informations ont été
            extraites depuis votre pièce d'identité (texte arabe OCR).
            Vérifiez et corrigez si nécessaire.
          </span>
        </div>
      }

      <form [formGroup]="form">
        <div formGroupName="informationsGenerales" class="space-y-6">

          <!-- ── Type de contribuable ── -->
          <div class="rounded-[24px] border border-slate-200 bg-slate-50 p-5">
            <label class="mb-3 block text-sm font-semibold text-slate-700">
              Type de contribuable <span class="text-red-500">*</span>
            </label>
            <div class="flex flex-wrap gap-4">
              <label
                class="flex cursor-pointer items-center gap-2 rounded-2xl border px-4 py-2.5 text-sm font-medium transition"
                [ngClass]="typeContribuable === 'PERSONNE_PHYSIQUE'
                  ? 'border-blue-500 bg-blue-50 text-blue-700'
                  : 'border-slate-200 bg-white text-slate-700 hover:border-blue-300'">
                <input type="radio" formControlName="typeContribuable"
                  value="PERSONNE_PHYSIQUE" class="h-4 w-4 text-blue-600" />
                Personne physique
              </label>
              <label
                class="flex cursor-pointer items-center gap-2 rounded-2xl border px-4 py-2.5 text-sm font-medium transition"
                [ngClass]="typeContribuable === 'PERSONNE_MORALE'
                  ? 'border-blue-500 bg-blue-50 text-blue-700'
                  : 'border-slate-200 bg-white text-slate-700 hover:border-blue-300'">
                <input type="radio" formControlName="typeContribuable"
                  value="PERSONNE_MORALE" class="h-4 w-4 text-blue-600" />
                Personne morale
              </label>
            </div>
          </div>

          <!-- ══ PERSONNE PHYSIQUE ══ -->
          @if (typeContribuable === 'PERSONNE_PHYSIQUE') {

            <div class="grid gap-4 md:grid-cols-2">

              <!-- Nom -->
              <div>
                <label class="label-field">
                  الاسم — Nom <span class="text-red-500">*</span>
                  @if (champPrerempli('nom')) {
                    <span class="ml-2 rounded-full bg-emerald-100 px-2 py-0.5 text-xs font-semibold text-emerald-700">
                      ✓ extrait
                    </span>
                  }
                </label>
                <input
                  type="text"
                  formControlName="nom"
                  placeholder="Votre nom"
                  [ngClass]="champPrerempli('nom') ? 'input-field input-prerempli' : 'input-field'"
                />
                @if (champInvalide('informationsGenerales.nom')) {
                  <p class="erreur-field">Le nom est requis</p>
                }
              </div>

              <!-- Prénom -->
              <div>
                <label class="label-field">
                  اللقب — Prénom <span class="text-red-500">*</span>
                  @if (champPrerempli('prenom')) {
                    <span class="ml-2 rounded-full bg-emerald-100 px-2 py-0.5 text-xs font-semibold text-emerald-700">
                      ✓ extrait
                    </span>
                  }
                </label>
                <input
                  type="text"
                  formControlName="prenom"
                  placeholder="Votre prénom"
                  [ngClass]="champPrerempli('prenom') ? 'input-field input-prerempli' : 'input-field'"
                />
                @if (champInvalide('informationsGenerales.prenom')) {
                  <p class="erreur-field">Le prénom est requis</p>
                }
              </div>

              <!-- CIN -->
              <div>
                <label class="label-field">
                  رقم بطاقة التعريف — Numéro CIN
                  @if (champPrerempli('cin')) {
                    <span class="ml-2 rounded-full bg-emerald-100 px-2 py-0.5 text-xs font-semibold text-emerald-700">
                      ✓ extrait
                    </span>
                  }
                </label>
                <input
                  type="text"
                  formControlName="cin"
                  placeholder="Ex: 12345678"
                  maxlength="8"
                  [ngClass]="champPrerempli('cin') ? 'input-field input-prerempli tracking-widest font-mono' : 'input-field'"
                />
                @if (champInvalide('informationsGenerales.cin')) {
                  <p class="erreur-field">Le CIN doit contenir 8 chiffres</p>
                }
              </div>

              <!-- Passeport -->
              <div>
                <label class="label-field">Numéro Passeport</label>
                <input
                  type="text"
                  formControlName="numeroPasseport"
                  placeholder="Ex: A1234567"
                  class="input-field"
                />
              </div>

              <!-- Date de naissance -->
              <div>
                <label class="label-field">
                  تاريخ الولادة — Date de naissance <span class="text-red-500">*</span>
                  @if (champPrerempli('dateNaissanceOuCreation')) {
                    <span class="ml-2 rounded-full bg-emerald-100 px-2 py-0.5 text-xs font-semibold text-emerald-700">
                      ✓ extrait
                    </span>
                  }
                </label>
                <input
                  type="date"
                  formControlName="dateNaissanceOuCreation"
                  [ngClass]="champPrerempli('dateNaissanceOuCreation') ? 'input-field input-prerempli' : 'input-field'"
                />
              </div>

            </div>

            <!-- Séparateur -->
            <div class="flex items-center gap-3">
              <div class="flex-1 border-t border-slate-200"></div>
              <span class="text-xs font-semibold text-slate-400 uppercase tracking-wide">
                Coordonnées
              </span>
              <div class="flex-1 border-t border-slate-200"></div>
            </div>
          }

          <!-- ══ PERSONNE MORALE ══ -->
          @if (typeContribuable === 'PERSONNE_MORALE') {
            <div class="grid gap-4 md:grid-cols-2">

              <div>
                <label class="label-field">Raison sociale <span class="text-red-500">*</span></label>
                <input type="text" formControlName="raisonSociale"
                  placeholder="Nom de l'entreprise" class="input-field" />
                @if (champInvalide('informationsGenerales.raisonSociale')) {
                  <p class="erreur-field">La raison sociale est requise</p>
                }
              </div>

              <div>
                <label class="label-field">Registre de commerce</label>
                <input type="text" formControlName="registreCommerce"
                  placeholder="N° registre de commerce" class="input-field" />
              </div>

              <div>
                <label class="label-field">Date de création de l'entreprise</label>
                <input type="date" formControlName="dateNaissanceOuCreation" class="input-field" />
              </div>

            </div>
          }

          <!-- ══ Champs communs : adresse, ville, CP, téléphone, email ══ -->
          <div class="grid gap-4 md:grid-cols-2">

            <div class="md:col-span-2">
              <label class="label-field">Adresse <span class="text-red-500">*</span></label>
              <input type="text" formControlName="adresse"
                placeholder="Adresse complète" class="input-field" />
              @if (champInvalide('informationsGenerales.adresse')) {
                <p class="erreur-field">L'adresse est requise</p>
              }
            </div>

            <div>
              <label class="label-field">Ville <span class="text-red-500">*</span></label>
              <input type="text" formControlName="ville"
                placeholder="Ex: Tunis" class="input-field" />
              @if (champInvalide('informationsGenerales.ville')) {
                <p class="erreur-field">La ville est requise</p>
              }
            </div>

            <div>
              <label class="label-field">Code postal <span class="text-red-500">*</span></label>
              <input type="text" formControlName="codePostal"
                placeholder="Ex: 1000" maxlength="4" class="input-field" />
              @if (champInvalide('informationsGenerales.codePostal')) {
                <p class="erreur-field">Code postal invalide (4 chiffres)</p>
              }
            </div>

            <div>
              <label class="label-field">Téléphone <span class="text-red-500">*</span></label>
              <input type="tel" formControlName="telephone"
                placeholder="Ex: 20123456" maxlength="8" class="input-field" />
              @if (champInvalide('informationsGenerales.telephone')) {
                <p class="erreur-field">Numéro invalide (8 chiffres)</p>
              }
            </div>

            <div>
              <label class="label-field">Email <span class="text-red-500">*</span></label>
              <input type="email" formControlName="email"
                placeholder="exemple@email.com" class="input-field" />
              @if (champInvalide('informationsGenerales.email')) {
                <p class="erreur-field">Email invalide</p>
              }
            </div>

          </div>

        </div><!-- /formGroupName -->
      </form>

      <!-- ── Navigation ── -->
      <div class="flex justify-between border-t border-slate-200 pt-6">
        <button
          type="button"
          (click)="wizardState.etapePrecedente()"
          class="rounded-2xl bg-slate-100 px-6 py-3 text-sm font-semibold text-slate-700 transition hover:bg-slate-200">
          ← Précédent
        </button>
        <button
          type="button"
          (click)="suivant()"
          [disabled]="!etapeValide()"
          class="rounded-2xl bg-blue-600 px-6 py-3 text-sm font-semibold text-white transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-60">
          Suivant →
        </button>
      </div>

    </div>
  `,
  styles: [`
    .label-field  { @apply mb-1 block text-sm font-semibold text-slate-700; }
    .erreur-field { @apply mt-1 text-xs text-red-500; }

    /* Champ standard */
    .input-field {
      @apply w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm
             outline-none transition
             focus:border-blue-500 focus:ring-2 focus:ring-blue-100;
    }

    /* Champ pré-rempli par OCR — bordure verte légère */
    .input-prerempli {
      @apply border-emerald-300 bg-emerald-50/40
             focus:border-emerald-500 focus:ring-emerald-100;
    }
  `],
})
export class StepInfosGeneralesComponent {

  readonly wizardState = inject(WizardStateService);
  readonly form = this.wizardState.formulaire;

  /** Valeurs initiales snapshot pour détecter ce qui a été pré-rempli par OCR. */
  private readonly valeursOcr = computed(() => {
    const piece = this.wizardState.piecesJointes().find(
      (p) => p.ocr?.statut === 'TERMINE'
    );
    return piece?.ocr?.champs ?? null;
  });

  /** true si au moins un champ a été extrait par OCR. */
  aDesChampsPreremplis(): boolean {
    const ocr = this.valeursOcr();
    return !!(ocr?.nom || ocr?.prenom || ocr?.cin);
  }

  /**
   * Retourne true si le champ a été rempli par OCR (pour afficher le badge
   * "✓ extrait" et la bordure verte).
   */
  champPrerempli(champ: 'nom' | 'prenom' | 'cin' | 'dateNaissanceOuCreation'): boolean {
    const ocr = this.valeursOcr();
    if (!ocr) return false;
    const valeurActuelle = this.form.get(`informationsGenerales.${champ}`)?.value;
    if (!valeurActuelle) return false;
    switch (champ) {
      case 'nom':                   return !!ocr.nom    && ocr.nom    === valeurActuelle;
      case 'prenom':                return !!ocr.prenom && ocr.prenom === valeurActuelle;
      case 'cin':                   return !!ocr.cin    && ocr.cin    === valeurActuelle;
      case 'dateNaissanceOuCreation': return !!ocr.dateNaissance && ocr.dateNaissance === valeurActuelle;
      default: return false;
    }
  }

  get typeContribuable(): string {
    return (
      this.form.get('informationsGenerales.typeContribuable')?.value ?? 'PERSONNE_PHYSIQUE'
    );
  }

  champInvalide(chemin: string): boolean {
    const ctrl = this.form.get(chemin);
    return !!(ctrl?.invalid && ctrl?.touched);
  }

  etapeValide(): boolean {
    const grp = this.form.get('informationsGenerales');
    if (!grp) return false;

    // Validation minimale manuelle pour nom/prénom (non marqués required dans le FormGroup)
    if (this.typeContribuable === 'PERSONNE_PHYSIQUE') {
      const nom    = grp.get('nom')?.value?.trim();
      const prenom = grp.get('prenom')?.value?.trim();
      if (!nom || !prenom) return false;
    }
    if (this.typeContribuable === 'PERSONNE_MORALE') {
      const rs = grp.get('raisonSociale')?.value?.trim();
      if (!rs) return false;
    }

    return grp.valid;
  }

  suivant(): void {
    const grp = this.form.get('informationsGenerales');
    grp?.markAllAsTouched();
    if (this.etapeValide()) {
      this.wizardState.etapeSuivante();
    }
  }
}