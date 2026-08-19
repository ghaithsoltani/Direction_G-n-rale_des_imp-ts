import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ChampsExtraitsOcr } from '../models/piece-jointe.model';

@Injectable({ providedIn: 'root' })
export class OcrService {
  private readonly http = inject(HttpClient);

  extraireInfos(piece: File, typePiece: string): Observable<ChampsExtraitsOcr> {
    const formData = new FormData();
    if (!['application/pdf', 'image/jpeg', 'image/jpg', 'image/png'].includes(piece.type)) {
      throw new Error('Format de document non pris en charge par l’OCR');
    }
    formData.append('fichier', piece, piece.name);
    // Arabic is the primary language on Tunisian identity cards; French is
    // retained for bilingual cards and French month names such as "mai".
    return this.http.post<unknown>(`${environment.apiBaseUrl}/ocr/extract`, formData, { params: { typePiece, languages: 'ara+fra' } }).pipe(
      map((reponse) => {
        // The API has returned both a flat object and { data/result: {...} } in
        // earlier versions. Keep the client compatible with either response.
        const resultat = this.obtenirResultat(reponse);
        const texteOcr = this.texte(resultat['texte'] ?? resultat['text'] ?? resultat['fullText'] ?? resultat['rawText']);
        const champsDepuisTexte = this.extraireDepuisTexteArabe(texteOcr);
        const champs: ChampsExtraitsOcr = {
          nom: this.texte(resultat['nomDetecte'] ?? resultat['nom'] ?? resultat['nomFamille'] ?? resultat['lastName']) ?? champsDepuisTexte.nom,
          prenom: this.texte(resultat['prenomDetecte'] ?? resultat['prenom'] ?? resultat['prenoms'] ?? resultat['firstName']) ?? champsDepuisTexte.prenom,
          cin: this.normaliserNumero(resultat['numeroPieceDetecte'] ?? resultat['numeroDocument'] ?? resultat['cin'] ?? resultat['numeroCin'] ?? resultat['documentNumber']) ?? champsDepuisTexte.cin,
          dateNaissance: this.normaliserDate(resultat['dateNaissanceDetectee'] ?? resultat['dateNaissance'] ?? resultat['dateOfBirth']) ?? champsDepuisTexte.dateNaissance,
          adresse: this.texte(resultat['adresse'] ?? resultat['address']),
        };

        if (!champs.nom && !champs.prenom && !champs.cin && !champs.dateNaissance) {
          throw new Error('Aucune information d’identité n’a été détectée dans ce document.');
        }
        return champs;
      })
    );
  }

  private obtenirResultat(reponse: unknown): Record<string, unknown> {
    if (!reponse || typeof reponse !== 'object') return {};
    const objet = reponse as Record<string, unknown>;
    const contenu = objet['data'] ?? objet['result'] ?? objet['resultat'];
    return contenu && typeof contenu === 'object' ? contenu as Record<string, unknown> : objet;
  }

  private texte(valeur: unknown): string | undefined {
    return typeof valeur === 'string' && valeur.trim() ? valeur.trim() : undefined;
  }

  private normaliserNumero(valeur: unknown): string | undefined {
    const texte = this.texte(valeur);
    return texte ? this.normaliserChiffres(texte).replace(/\s/g, '') : undefined;
  }

  /** Extracts fields from raw Arabic OCR text when the OCR provider does not structure its response. */
  private extraireDepuisTexteArabe(texteOcr?: string): ChampsExtraitsOcr {
    if (!texteOcr) return {};
    const texte = texteOcr.replace(/\r/g, '\n').replace(/[ \t]+/g, ' ');
    const lireChamp = (etiquette: RegExp, fin: RegExp): string | undefined => {
      const correspondance = texte.match(etiquette);
      if (!correspondance?.[1]) return undefined;
      return this.texte(correspondance[1].replace(fin, '').trim());
    };
    const nom = lireChamp(/(?:اللقب|الاسم العائلي)\s*[:：]?\s*([^\n]+)/u, /(?:\s+(?:الاسم|تاريخ الولادة|مكانها)\b.*)?$/u);
    const prenom = lireChamp(/(?:^|\n)\s*الاسم\s*[:：]?\s*([^\n]+)/mu, /(?:\s+(?:تاريخ الولادة|مكانها|اللقب)\b.*)?$/u);
    const cin = this.normaliserChiffres(texte).match(/(?<!\d)\d{8}(?!\d)/)?.[0];
    const date = this.trouverDateDansTexte(texte);
    return { nom, prenom, cin, dateNaissance: date };
  }

  private normaliserDate(valeur: unknown): string | undefined {
    if (typeof valeur !== 'string' || !valeur.trim()) return undefined;
    const date = this.normaliserChiffres(valeur).trim().toLowerCase();
    const iso = date.match(/^(\d{4})[-/.](\d{1,2})[-/.](\d{1,2})$/);
    if (iso) return `${iso[1]}-${iso[2].padStart(2, '0')}-${iso[3].padStart(2, '0')}`;
    const fr = date.match(/^(\d{1,2})[-/.](\d{1,2})[-/.](\d{4})$/);
    if (fr) return `${fr[3]}-${fr[2].padStart(2, '0')}-${fr[1].padStart(2, '0')}`;
    const mois: Record<string, string> = {
      'جانفي': '01', 'يناير': '01', 'فيفري': '02', 'فبراير': '02', 'مارس': '03', 'أفريل': '04', 'ابريل': '04', 'avril': '04',
      'ماي': '05', 'mai': '05', 'جوان': '06', 'يونيو': '06', 'juin': '06', 'جويلية': '07', 'يوليو': '07', 'juillet': '07',
      'أوت': '08', 'اوت': '08', 'أغسطس': '08', 'septembre': '09', 'سبتمبر': '09', 'octobre': '10', 'أكتوبر': '10',
      'novembre': '11', 'نوفمبر': '11', 'décembre': '12', 'decembre': '12', 'ديسمبر': '12'
    };
    const textuelle = date.match(/^(\d{1,2})\s+([^\s]+)\s+(\d{4})$/u);
    const moisNumero = textuelle ? mois[textuelle[2]] : undefined;
    return textuelle && moisNumero ? `${textuelle[3]}-${moisNumero}-${textuelle[1].padStart(2, '0')}` : undefined;
  }

  private trouverDateDansTexte(texte: string): string | undefined {
    const chiffres = this.normaliserChiffres(texte);
    const correspondance = chiffres.match(/\b\d{1,2}(?:[-/.]\d{1,2}[-/.]|\s+[^\s\n]+\s+)\d{4}\b/u);
    return correspondance ? this.normaliserDate(correspondance[0]) : undefined;
  }

  private normaliserChiffres(valeur: string): string {
    return valeur.replace(/[٠-٩۰-۹]/g, (chiffre) => {
      const index = '٠١٢٣٤٥٦٧٨٩۰۱۲۳۴۵۶۷۸۹'.indexOf(chiffre);
      return String(index % 10);
    });
  }
}
