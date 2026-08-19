import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * Bloque l'accès aux routes protégées si l'utilisateur n'est pas connecté.
 * Redirige vers la page de login en conservant l'URL cible (returnUrl)
 * pour y rediriger l'utilisateur après connexion.
 */
export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.estConnecte()) {
    return true;
  }

  return router.createUrlTree(['/auth/login'], {
    queryParams: { returnUrl: state.url },
  });
};