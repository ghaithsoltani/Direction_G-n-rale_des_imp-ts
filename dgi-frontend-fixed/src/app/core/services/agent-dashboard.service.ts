import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DashboardActivite, DashboardNotification, DashboardStats } from '../models/agent-dashboard.model';
import { DossierImmatriculation, FiltresDossier, PageResultat } from '../models/dossier-immatriculation.model';
import { StatutDossier } from '../models/statut-dossier.enum';

@Injectable({ providedIn: 'root' })
export class AgentDashboardService {
  private readonly http = inject(HttpClient);

  /**
   * FIX 8: l'URL était codée en dur 'http://localhost:8081/api' au lieu d'utiliser
   * environment.apiBaseUrl, ce qui cassait les builds de prod et l'utilisation du proxy Angular.
   */
  listerDossiers(filtres: FiltresDossier): Observable<PageResultat<DossierImmatriculation>> {
    let params = new HttpParams()
      .set('page', String(filtres.page))
      .set('size', String(filtres.taille));
    if (filtres.statut)    params = params.set('statut', filtres.statut);
    if (filtres.recherche) params = params.set('recherche', filtres.recherche);

    return this.http.get<PageResultat<DossierImmatriculation>>(
      `${environment.apiBaseUrl}/dossiers`,
      { params }
    );
  }
}
