import { Routes } from '@angular/router';

export const IMMATRICULATION_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./immatriculation-wizard/immatriculation-wizard.component').then(m => m.ImmatriculationWizardComponent),
  },
];
