import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DossierImmatriculation, FiltresDossier, PageResultat } from '../models/dossier-immatriculation.model';
import { PieceJointe, TypePieceJointe } from '../models/piece-jointe.model';
import { StatutDossier } from '../models/statut-dossier.enum';
import { StatistiquesDashboard } from '../models/statistiques-dashboard.model';

export interface DossierCreatePayload {
  contribuableId: string;
}

@Injectable({ providedIn: 'root' })
export class DossierService {
  private readonly http = inject(HttpClient);

  creer(payload: DossierCreatePayload): Observable<DossierImmatriculation> {
    return this.http.post<DossierImmatriculation>(`${environment.apiBaseUrl}/dossiers`, payload);
  }

  obtenirParId(id: string): Observable<DossierImmatriculation> {
    return this.http.get<DossierImmatriculation>(`${environment.apiBaseUrl}/dossiers/${id}`);
  }

  /**
   * La ressource du dossier ne contient qu'un résumé des pièces.  L'endpoint
   * dédié porte notamment l'URL de consultation nécessaire à l'aperçu.
   */
  obtenirDocuments(id: string): Observable<PieceJointe[]> {
    return this.http
      .get<unknown>(`${environment.apiBaseUrl}/dossiers/${id}/documents`)
      .pipe(map((response) => this.normaliserDocuments(response)));
  }

  lister(filtres: FiltresDossier): Observable<PageResultat<DossierImmatriculation>> {
    let params = new HttpParams()
      .set('page', filtres.page.toString())
      .set('size', filtres.taille.toString());

    if (filtres.statut)    params = params.set('statut', filtres.statut);
    if (filtres.recherche) params = params.set('recherche', filtres.recherche);

    return this.http.get<PageResultat<DossierImmatriculation>>(
      `${environment.apiBaseUrl}/dossiers`, { params }
    ).pipe(
      map((page) => ({
        content: page.content ?? page.contenu ?? [],
        contenu: page.contenu ?? page.content ?? [],
        totalElements: page.totalElements ?? page.totalElementsCount ?? 0,
        totalPages: page.totalPages ?? 1,
        pageActuelle: page.pageActuelle ?? page.number ?? 0,
        size: page.size ?? filtres.taille,
      }))
    );
  }

  /** Statistiques du dashboard agent (compteurs + évolution mensuelle réelle). */
  obtenirStatistiques(): Observable<StatistiquesDashboard> {
    return this.http.get<StatistiquesDashboard>(`${environment.apiBaseUrl}/dossiers/statistiques`);
  }

  soumettre(id: string): Observable<DossierImmatriculation> {
    return this.http.post<DossierImmatriculation>(
      `${environment.apiBaseUrl}/dossiers/${id}/soumettre`, null
    );
  }

  obtenirParContribuable(contribuableId: string): Observable<DossierImmatriculation[]> {
    return this.http.get<DossierImmatriculation[]>(
      `${environment.apiBaseUrl}/dossiers/contribuable/${contribuableId}`
    );
  }

  changerStatut(id: string, statut: StatutDossier, commentaire?: string): Observable<DossierImmatriculation> {
    return this.http.put<DossierImmatriculation>(`${environment.apiBaseUrl}/dossiers/${id}/statut`, {
      nouveauStatut: statut,
      commentaire,
    });
  }

  private normaliserDocuments(response: unknown): PieceJointe[] {
    const documents = Array.isArray(response)
      ? response
      : this.lireTableau(response, ['content', 'documents', 'piecesJointes', 'data']);

    return documents.map((document, index) => {
      const source = document as Record<string, unknown>;
      const nomFichier = this.lireTexte(source, ['nomFichier', 'fileName', 'nom', 'name']) || `Document ${index + 1}`;
      const type = this.lireTexte(source, ['type', 'typePiece', 'typeDocument']) || 'AUTRE';
      const url = this.lireTexte(source, ['url', 'urlFichier', 'fileUrl', 'documentUrl', 'downloadUrl', 'lien']);

      return {
        id: this.lireTexte(source, ['id', 'documentId']),
        type: type as TypePieceJointe,
        nomFichier,
        url: url ? this.resoudreUrl(url) : undefined,
        tailleOctets: this.lireNombre(source, ['tailleOctets', 'taille', 'size']),
        dateUpload: this.lireTexte(source, ['dateUpload', 'uploadedAt', 'dateCreation']),
      };
    });
  }

  private lireTableau(response: unknown, cles: string[]): unknown[] {
    if (!response || typeof response !== 'object') return [];
    const source = response as Record<string, unknown>;
    for (const cle of cles) {
      if (Array.isArray(source[cle])) return source[cle] as unknown[];
    }
    return [];
  }

  private lireTexte(source: Record<string, unknown>, cles: string[]): string | undefined {
    for (const cle of cles) {
      const valeur = source[cle];
      if (typeof valeur === 'string' && valeur.trim()) return valeur;
    }
    return undefined;
  }

  private lireNombre(source: Record<string, unknown>, cles: string[]): number | undefined {
    for (const cle of cles) {
      const valeur = source[cle];
      if (typeof valeur === 'number') return valeur;
    }
    return undefined;
  }

  private resoudreUrl(url: string): string {
    if (/^(https?:|\/\/|blob:|data:)/i.test(url)) return url;
    return new URL(url, `${environment.apiBaseUrl}/`).toString();
  }
}
