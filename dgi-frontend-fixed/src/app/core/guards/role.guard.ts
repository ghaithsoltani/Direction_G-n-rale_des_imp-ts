import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { RoleUtilisateur } from '../models/utilisateur.model';

/**
 * Vérifie que le rôle de l'utilisateur connecté figure dans la liste des
 * rôles autorisés déclarée sur la route via `data: { roles: [...] }`.
 * Doit être utilisé APRÈS authGuard (l'utilisateur est supposé connecté).
 */
export const roleGuard: CanActivateFn = (route) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const rolesAutorises = route.data['roles'] as RoleUtilisateur[] | undefined;
  const utilisateur = authService.utilisateurCourant();

  if (!rolesAutorises || rolesAutorises.length === 0) {
    return true; // aucune restriction de rôle définie sur cette route
  }

  if (utilisateur && rolesAutorises.includes(utilisateur.role)) {
    return true;
  }

  // Connecté mais rôle non autorisé -> redirection vers une page neutre
  return router.createUrlTree(['/auth/login']);
};