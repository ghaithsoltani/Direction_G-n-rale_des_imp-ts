import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Contribuable } from '../models/contribuable.model';
import { Activite } from '../models/activite.model';

@Injectable({ providedIn: 'root' })
export class ContribuableService {
  private readonly http = inject(HttpClient);

  /**
   * FIX 7: Le backend utilise @JsonTypeInfo avec property="type" (pas "typeContribuable").
   * Les champs dateNaissanceOuCreation doivent être mappés selon le type :
   *   - PERSONNE_PHYSIQUE -> dateNaissance
   *   - PERSONNE_MORALE   -> dateCreationEntreprise
   * L'AdresseDTO attend: rue, ville, codePostal.
   */
  creer(contribuable: Contribuable, activite?: Activite): Observable<Contribuable> {
    const estPM = contribuable.typeContribuable === 'PERSONNE_MORALE';

    const adresse = {
      rue: contribuable.adresse,
      ville: contribuable.ville,
      codePostal: contribuable.codePostal,
    };

    const activitePayload = activite
      ? {
          codeActivitePrincipale: activite.codeActivitePrincipale,
          libelleActivite: activite.libelleActivite,
          secteurActivite: activite.secteurActivite,
          dateDebutActivite: activite.dateDebutActivite,
          adresseExercice: activite.adresseExercice,
          activitePrincipale: true,
        }
      : undefined;

    const payloadBase = {
      type: contribuable.typeContribuable, // FIX: 'type' requis par @JsonTypeInfo, pas 'typeContribuable'
      cin: contribuable.cin,
      numeroPasseport: contribuable.numeroPasseport,
      email: contribuable.email,
      telephone: contribuable.telephone,
      adresse,
      activite: activitePayload,
    };

    const payload = estPM
      ? {
          ...payloadBase,
          raisonSociale: contribuable.raisonSociale,
          registreCommerce: contribuable.registreCommerce,
          dateCreationEntreprise: contribuable.dateNaissanceOuCreation, // FIX: nom de champ PM
        }
      : {
          ...payloadBase,
          nom: contribuable.nom,
          prenom: contribuable.prenom,
          dateNaissance: contribuable.dateNaissanceOuCreation, // FIX: nom de champ PP
        };

    return this.http.post<Contribuable>(`${environment.apiBaseUrl}/contribuables`, payload);
  }

  obtenirParId(id: string): Observable<Contribuable> {
    return this.http.get<Contribuable>(`${environment.apiBaseUrl}/contribuables/${id}`);
  }

  obtenirParCin(cin: string): Observable<Contribuable> {
    return this.http.get<Contribuable>(`${environment.apiBaseUrl}/contribuables/recherche`, {
      params: { cin },
    });
  }

  lister(page: number, taille: number): Observable<{ contenu: Contribuable[]; totalElements: number }> {
    return this.http.get<{ contenu: Contribuable[]; totalElements: number }>(
      `${environment.apiBaseUrl}/contribuables`,
      { params: { page, taille } }
    );
  }
}
