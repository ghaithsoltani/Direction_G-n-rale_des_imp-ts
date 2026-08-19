import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';
import { AppShellComponent } from './shared/components/app-shell/app-shell.component';

export const routes: Routes = [
  { path: '', loadComponent: () => import('./features/home/home.component').then(m => m.HomeComponent) },
  {
    path: 'auth',
    loadChildren: () => import('./features/auth/auth.routes').then(m => m.AUTH_ROUTES),
  },
  {
    path: '',
    component: AppShellComponent,
    children: [
      {
        path: 'immatriculation',
        canActivate: [authGuard, roleGuard],
        data: { roles: ['CONTRIBUABLE', 'ADMIN'] },
        loadChildren: () => import('./features/immatriculation/immatriculation.routes').then(m => m.IMMATRICULATION_ROUTES),
      },
      {
        path: 'agent',
        canActivate: [authGuard, roleGuard],
        data: { roles: ['AGENT_DGI', 'ADMIN'] },
        loadChildren: () => import('./features/agent-dgi/agent-dgi.routes').then(m => m.AGENT_DGI_ROUTES),
      },
      {
        path: 'dashboard',
        canActivate: [authGuard],
        loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent),
      },
      {
        path: 'notifications',
        canActivate: [authGuard],
        loadComponent: () => import('./features/notifications/notifications.component').then(m => m.NotificationsComponent),
      },
      {
        path: 'help',
        canActivate: [authGuard],
        loadComponent: () => import('./features/help/help.component').then(m => m.HelpComponent),
      },
      {
        path: 'profile',
        canActivate: [authGuard],
        loadComponent: () => import('./features/profile/profile.component').then(m => m.ProfileComponent),
      },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
    ],
  },
  { path: '**', redirectTo: 'auth/login' },
];
