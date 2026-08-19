import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

/**
 * Injecte le JWT dans chaque requête sortante vers l'API DGI.
 * Le choix du bon token selon le rôle est délégué à AuthService.getToken()
 * pour éviter la duplication de logique et les erreurs silencieuses.
 *
 * FIX 3a: suppression du switch manuel de token par URL dans l'intercepteur —
 * il causait l'envoi du mauvais token (ex: agentToken sur une route contribuable).
 *
 * FIX 3b: la déconnexion sur 401/403 est gérée ici uniquement (l'error.interceptor
 * n'appelle plus logout pour éviter la double redirection).
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken();

  const requeteAvecToken = token
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(requeteAvecToken).pipe(
    catchError((erreur: HttpErrorResponse) => {
      if (erreur.status === 401) {
        authService.logout();
      }
      return throwError(() => erreur);
    })
  );
};
