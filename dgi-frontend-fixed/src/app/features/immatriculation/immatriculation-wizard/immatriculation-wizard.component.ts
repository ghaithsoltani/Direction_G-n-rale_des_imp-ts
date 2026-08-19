import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { WizardStateService } from '../services/wizard-state.service';
import { StepInfosGeneralesComponent } from '../steps/step-infos-generales/step-infos-generales.component';
import { StepActiviteComponent } from '../steps/step-activite/step-activite.component';
import { StepPiecesJointesComponent } from '../steps/step-pieces-jointes/step-pieces-jointes.component';
import { StepWebcamComponent } from '../steps/step-webcam/step-webcam.component';
import { StepRecapitulatifComponent } from '../steps/step-recapitulatif/step-recapitulatif.component';

@Component({
  selector: 'app-immatriculation-wizard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    StepInfosGeneralesComponent,
    StepActiviteComponent,
    StepPiecesJointesComponent,
    StepWebcamComponent,
    StepRecapitulatifComponent,
  ],
  template: `
    <div class="space-y-6">
      <header class="rounded-[32px] border border-slate-200 bg-gradient-to-br from-slate-950 via-blue-900 to-blue-700 p-6 text-white shadow-[0_20px_60px_rgba(15,23,42,0.16)]">
        <div class="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <p class="text-sm uppercase tracking-[0.3em] text-blue-100">Immatriculation fiscale</p>
            <h1 class="mt-2 text-2xl font-semibold">Démarrez votre demande en ligne</h1>
            <p class="mt-2 max-w-2xl text-sm text-blue-100">Le parcours a été refondu pour être plus fluide, accessible et adapté à une expérience gouvernementale moderne.</p>
          </div>
          <div class="rounded-2xl border border-white/15 bg-white/10 px-4 py-3 text-sm">
            <p class="font-semibold">Étape {{ wizardState.etapeCourante() }} / {{ wizardState.totalEtapes }}</p>
            <p class="text-blue-100">{{ etapes[wizardState.etapeCourante() - 1].label }}</p>
          </div>
        </div>
      </header>

      @if (wizardState.brouillonRestaure() || wizardState.derniereSauvegarde()) {
        <div class="flex flex-wrap items-center gap-2 rounded-2xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-800">
          <span class="font-semibold">✓ Brouillon enregistré automatiquement</span>
          @if (wizardState.brouillonRestaure()) { <span>Vos informations ont été restaurées.</span> }
        </div>
      }

      <div class="rounded-[24px] border border-slate-200 bg-white p-4 shadow-sm sm:p-6">
        <div class="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
          @for (etape of etapes; track etape.numero) {
            <div class="flex items-center gap-3" [class.flex-1]="!$last">
              <div class="flex h-10 w-10 items-center justify-center rounded-full text-sm font-semibold transition-all" [ngClass]="{
                'bg-blue-600 text-white shadow-lg': wizardState.etapeCourante() === etape.numero,
                'bg-emerald-500 text-white': wizardState.etapeCourante() > etape.numero,
                'bg-slate-100 text-slate-500': wizardState.etapeCourante() < etape.numero
              }">
                @if (wizardState.etapeCourante() > etape.numero) {
                  ✓
                } @else {
                  {{ etape.numero }}
                }
              </div>
              <div>
                <p class="text-sm font-semibold" [ngClass]="{
                  'text-blue-700': wizardState.etapeCourante() === etape.numero,
                  'text-emerald-600': wizardState.etapeCourante() > etape.numero,
                  'text-slate-500': wizardState.etapeCourante() < etape.numero
                }">{{ etape.label }}</p>
              </div>
              @if (!$last) {
                <div class="hidden h-1 flex-1 rounded-full bg-slate-200 lg:block" [ngClass]="{'bg-emerald-500': wizardState.etapeCourante() > etape.numero}"></div>
              }
            </div>
          }
        </div>
      </div>

      <div class="rounded-[24px] border border-slate-200 bg-white p-6 shadow-sm sm:p-8">
        @switch (wizardState.etapeCourante()) {
          @case (1) { <app-step-pieces-jointes /> }
          @case (2) { <app-step-infos-generales /> }
          @case (3) { <app-step-activite /> }
          @case (4) { <app-step-webcam /> }
          @case (5) { <app-step-recapitulatif /> }
        }
      </div>
    </div>
  `,
})
export class ImmatriculationWizardComponent {
  readonly wizardState = inject(WizardStateService);

  readonly etapes = [
    { numero: 1, label: 'Extraction des documents' },
    { numero: 2, label: 'Infos générales' },
    { numero: 3, label: 'Activité' },
    { numero: 4, label: 'Vérification' },
    { numero: 5, label: 'Récapitulatif' },
  ];
}
