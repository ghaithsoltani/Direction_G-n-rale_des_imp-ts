import { Routes } from '@angular/router';

export const AGENT_DGI_ROUTES: Routes = [
  {
    path: 'dossiers',
    loadComponent: () => import('./dossiers-liste/dossiers-liste.component').then(m => m.DossiersListeComponent),
  },
  {
    path: 'dossiers/:id',
    loadComponent: () => import('./dossier-detail/dossier-detail.component').then(m => m.DossierDetailComponent),
  },
  { path: '', redirectTo: 'dossiers', pathMatch: 'full' },
];
