import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { ToastService } from '../services/toast.service';

/**
 * Intercepteur global d'erreurs HTTP : affiche une notification toast
 * compréhensible pour l'usager selon le code d'erreur.
 *
 * FIX 4: suppression du double appel logout()/window.location.reload() ici.
 * La déconnexion est gérée par auth.interceptor uniquement pour éviter une
 * double redirection et des erreurs "Navigation already in progress".
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const toastService = inject(ToastService);

  return next(req).pipe(
    catchError((erreur: HttpErrorResponse) => {
      // La page de connexion présente elle-même un message adapté : éviter
      // l'affichage de deux notifications pour une seule tentative.
      if (req.url.endsWith('/auth/login')) {
        return throwError(() => erreur);
      }

      const backendMessage = extraireMessageErreur(erreur);
      const message = backendMessage ?? messageErreurPourStatut(erreur);
      toastService.afficherErreur(message);
      return throwError(() => erreur);
    })
  );
};

function extraireMessageErreur(erreur: HttpErrorResponse): string | null {
  const body = erreur.error;
  if (!body || typeof body !== 'object') return null;

  if (typeof body.message === 'string' && body.message.trim()) return body.message;

  if (Array.isArray(body.erreursChamps) && body.erreursChamps.length > 0) {
    return body.erreursChamps
      .map((item: { champ?: string; message?: string }) => item.message ?? item.champ)
      .filter(Boolean)
      .join(' • ');
  }
  return null;
}

function messageErreurPourStatut(erreur: HttpErrorResponse): string {
  switch (erreur.status) {
    case 0:    return 'Impossible de contacter le serveur. Vérifiez votre connexion.';
    case 400:  return erreur.error?.message ?? 'Requête invalide.';
    case 401:  return 'Votre session a expiré. Veuillez vous reconnecter.';
    case 403:  return "Vous n'avez pas les droits nécessaires pour cette action.";
    case 404:  return 'Ressource introuvable.';
    case 413:  return 'Le fichier envoyé est trop volumineux (max 15 Mo).';
    case 500:  return 'Une erreur serveur est survenue. Réessayez plus tard.';
    default:   return erreur.error?.message ?? 'Une erreur inattendue est survenue.';
  }
}
