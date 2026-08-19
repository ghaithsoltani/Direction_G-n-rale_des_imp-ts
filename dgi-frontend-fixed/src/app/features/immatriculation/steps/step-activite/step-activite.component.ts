import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { WizardStateService } from '../../services/wizard-state.service';

@Component({
  selector: 'app-step-activite',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="space-y-6">
      <div class="rounded-2xl border border-blue-100 bg-blue-50/70 p-4">
        <h2 class="text-2xl font-semibold text-slate-900">Activité professionnelle</h2>
        <p class="mt-2 text-sm text-slate-600">Décrivez votre activité avec les informations nécessaires au traitement du dossier.</p>
      </div>

      <form [formGroup]="form">
        <div formGroupName="activite" class="space-y-4">
          <div class="grid gap-4 md:grid-cols-2">
            <div>
              <label class="label-field">Secteur d'activité <span class="text-red-500">*</span></label>
              <select formControlName="secteurActivite" class="input-field">
                <option value="">-- Sélectionner --</option>
                <option value="COMMERCE">Commerce</option>
                <option value="INDUSTRIE">Industrie</option>
                <option value="SERVICES">Services</option>
                <option value="AGRICULTURE">Agriculture</option>
                <option value="ARTISANAT">Artisanat</option>
                <option value="PROFESSIONS_LIBERALES">Professions libérales</option>
                <option value="AUTRE">Autre</option>
              </select>
              @if (champInvalide('activite.secteurActivite')) {
                <p class="erreur-field">Le secteur est requis</p>
              }
            </div>

            <div>
              <label class="label-field">Code activité principale <span class="text-red-500">*</span></label>
              <input type="text" formControlName="codeActivitePrincipale" class="input-field" placeholder="Ex: 4711" />
              @if (champInvalide('activite.codeActivitePrincipale')) {
                <p class="erreur-field">Le code activité est requis</p>
              }
            </div>

            <div class="md:col-span-2">
              <label class="label-field">Libellé de l'activité <span class="text-red-500">*</span></label>
              <input type="text" formControlName="libelleActivite" class="input-field" placeholder="Description de l'activité principale" />
              @if (champInvalide('activite.libelleActivite')) {
                <p class="erreur-field">Le libellé est requis</p>
              }
            </div>

            <div>
              <label class="label-field">Régime fiscal <span class="text-red-500">*</span></label>
              <select formControlName="regimeFiscal" class="input-field">
                <option value="REEL">Régime réel</option>
                <option value="FORFAITAIRE">Régime forfaitaire</option>
                <option value="SIMPLIFIE">Régime simplifié</option>
              </select>
            </div>

            <div>
              <label class="label-field">Date de début d'activité <span class="text-red-500">*</span></label>
              <input type="date" formControlName="dateDebutActivite" class="input-field" />
              @if (champInvalide('activite.dateDebutActivite')) {
                <p class="erreur-field">La date de début est requise</p>
              }
            </div>

            <div class="md:col-span-2">
              <label class="label-field">Adresse d'exercice <span class="text-red-500">*</span></label>
              <input type="text" formControlName="adresseExercice" class="input-field" placeholder="Adresse du lieu d'activité" />
              @if (champInvalide('activite.adresseExercice')) {
                <p class="erreur-field">L'adresse d'exercice est requise</p>
              }
            </div>

            <div>
              <label class="label-field">Ville d'exercice <span class="text-red-500">*</span></label>
              <input type="text" formControlName="villeExercice" class="input-field" placeholder="Ex: Sfax" />
              @if (champInvalide('activite.villeExercice')) {
                <p class="erreur-field">La ville est requise</p>
              }
            </div>
          </div>
        </div>
      </form>

      <div class="flex flex-wrap justify-between gap-3 border-t border-slate-200 pt-6">
        <button (click)="wizardState.etapePrecedente()" class="rounded-2xl bg-slate-100 px-6 py-3 text-sm font-semibold text-slate-700 transition hover:bg-slate-200">← Précédent</button>
        <button (click)="suivant()" [disabled]="!etapeValide()" class="rounded-2xl bg-blue-600 px-6 py-3 text-sm font-semibold text-white transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-60">Suivant →</button>
      </div>
    </div>
  `,
  styles: [`
    .label-field { @apply mb-1 block text-sm font-semibold text-slate-700; }
    .input-field { @apply w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-100; }
    .erreur-field { @apply mt-1 text-xs text-red-500; }
  `]
})
export class StepActiviteComponent {
  readonly wizardState = inject(WizardStateService);
  readonly form = this.wizardState.formulaire;

  champInvalide(chemin: string): boolean {
    const ctrl = this.form.get(chemin);
    return !!(ctrl?.invalid && ctrl?.touched);
  }

  etapeValide(): boolean {
    return this.form.get('activite')?.valid ?? false;
  }

  suivant(): void {
    this.form.get('activite')?.markAllAsTouched();
    if (this.etapeValide()) this.wizardState.etapeSuivante();
  }
}
