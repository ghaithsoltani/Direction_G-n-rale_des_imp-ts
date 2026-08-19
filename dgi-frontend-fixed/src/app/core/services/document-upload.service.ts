import { HttpClient, HttpEvent, HttpEventType, HttpHeaders } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';

/** Types MIME acceptés par le backend DocumentUploadServiceImpl */
const TYPES_ACCEPTES = ['image/jpeg', 'image/jpg', 'image/png', 'application/pdf'];

/** Taille maximale : 10 Mo (configurée côté backend) */
const TAILLE_MAX_OCTETS = 10 * 1024 * 1024;

@Injectable({ providedIn: 'root' })
export class DocumentUploadService {
  private readonly http = inject(HttpClient);

  /** Upload avec suivi de progression (retourne 0-100%) */
  uploaderPiece(dossierId: string, typePiece: string, fichier: File): Observable<number> {
    this.validerFichier(fichier);
    const formData = new FormData();
    formData.append('fichier', fichier, fichier.name);

    return this.http
      .post<HttpEvent<unknown>>(
        `${environment.apiBaseUrl}/dossiers/${dossierId}/documents`,
        formData,
        {
          headers: new HttpHeaders({ Accept: 'application/json' }),
          observe: 'events',
          reportProgress: true,
          params: { typePiece },
        }
      )
      .pipe(
        map((event) => {
          if (event.type === HttpEventType.UploadProgress && event.total) {
            return Math.round((event.loaded / event.total) * 100);
          }
          if (event.type === HttpEventType.Response) {
            return 100;
          }
          return 0;
        })
      );
  }

  /** Upload simple sans suivi de progression */
  uploaderPieceSimple(dossierId: string, typePiece: string, fichier: File): Observable<unknown> {
    this.validerFichier(fichier);
    const formData = new FormData();
    formData.append('fichier', fichier, fichier.name);

    return this.http.post<unknown>(
      `${environment.apiBaseUrl}/dossiers/${dossierId}/documents`,
      formData,
      { params: { typePiece } }
    );
  }

  /**
   * FIX 6: Validation côté client alignée sur le backend.
   * Accepte image/jpeg, image/jpg, image/png et application/pdf.
   * L'ancienne version rejetait les PDF, causant une erreur silencieuse
   * pour les pièces comme le registre de commerce ou le justificatif de domicile.
   */
  private validerFichier(fichier: File): void {
    if (!TYPES_ACCEPTES.includes(fichier.type)) {
      throw new Error(`Format non autorisé (${fichier.type}). Formats acceptés : JPG, PNG, PDF.`);
    }
    if (fichier.size > TAILLE_MAX_OCTETS) {
      throw new Error(`Fichier trop volumineux (max 10 Mo, reçu : ${(fichier.size / 1024 / 1024).toFixed(1)} Mo).`);
    }
  }
}
