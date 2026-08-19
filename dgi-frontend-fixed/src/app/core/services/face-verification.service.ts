import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ResultatVerificationFaciale } from '../models/resultat-verification-faciale.model';

@Injectable({ providedIn: 'root' })
export class FaceVerificationService {
  private readonly http = inject(HttpClient);

  verifier(dossierId: string, pieceJointeReferenceId: string, photoLive: Blob): Observable<ResultatVerificationFaciale> {
    const formData = new FormData();
    formData.append('photoLive', photoLive, 'capture.jpg');

    return this.http.post<any>(`${environment.apiBaseUrl}/face/verify`, formData,{params:{dossierId,pieceJointeReferenceId}}).pipe(
      map((resultat) => ({
        scoreSimilarite: resultat?.scoreSimilarite ?? resultat?.score ?? 0,
        verifie: resultat?.correspondanceValidee ?? resultat?.verifie ?? false,
        message: resultat?.messageErreur ?? resultat?.message ?? undefined,
      }))
    );
  }
}
