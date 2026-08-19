import { Component, inject, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { DossierService } from '../../../core/services/dossier.service';
import { ToastService } from '../../../core/services/toast.service';
import { DossierImmatriculation } from '../../../core/models/dossier-immatriculation.model';
import { StatutDossier } from '../../../core/models/statut-dossier.enum';
import { PieceJointe } from '../../../core/models/piece-jointe.model';
import { IconComponent } from '../../../shared/components/icon/icon.component';

@Component({
  selector: 'app-dossier-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, IconComponent],
  template: `
    <div class="space-y-6">
      <header class="rounded-[28px] border border-slate-200 bg-gradient-to-br from-slate-950 via-blue-900 to-blue-700 p-6 text-white shadow-[0_20px_60px_rgba(15,23,42,0.16)]">
        <p class="text-sm uppercase tracking-[0.3em] text-blue-100">Dossier détaillé</p>
        <h1 class="mt-2 text-2xl font-semibold">{{ dossier()?.numeroDossier || 'Dossier #' + (dossier()?.id || '—') }}</h1>
        <p class="mt-2 max-w-2xl text-sm text-blue-100">Consultation en profondeur du dossier et des pièces justificatives.</p>
      </header>

      @if (dossier()) {
        <section class="grid gap-4 xl:grid-cols-[1.2fr_0.8fr]">
          <div class="rounded-[24px] border border-slate-200 bg-white p-6 shadow-sm">
            <p class="text-sm font-semibold text-blue-600">Contribuable</p>
            <h2 class="mt-2 text-xl font-semibold text-slate-900">
              {{ dossier()!.contribuable.nomAffichage || dossier()!.contribuable.raisonSociale || ((dossier()!.contribuable.nom || '') + ' ' + (dossier()!.contribuable.prenom || '')).trim() || 'Contribuable non renseigné' }}
            </h2>
            <p class="mt-3 text-sm text-slate-600">{{ dossier()!.contribuable.email || 'Email non renseigné' }} · {{ dossier()!.contribuable.telephone || 'Téléphone non renseigné' }}</p>
            <div class="mt-5 grid gap-3 sm:grid-cols-2">
              <div class="rounded-2xl bg-slate-50 p-3">
                <p class="text-xs uppercase tracking-[0.2em] text-slate-500">Statut</p>
                <p class="mt-1 text-sm font-semibold text-slate-900">{{ getBadgeLabel(dossier()!.statut) }}</p>
              </div>
              <div class="rounded-2xl bg-slate-50 p-3">
                <p class="text-xs uppercase tracking-[0.2em] text-slate-500">Date de soumission</p>
                <p class="mt-1 text-sm text-slate-700">{{ formatDate(dossier()!.dateSoumission) }}</p>
              </div>
              <div class="rounded-2xl bg-slate-50 p-3">
                <p class="text-xs uppercase tracking-[0.2em] text-slate-500">Date de création</p>
                <p class="mt-1 text-sm text-slate-700">{{ formatDate(dossier()!.dateCreation) }}</p>
              </div>
              <div class="rounded-2xl bg-slate-50 p-3">
                <p class="text-xs uppercase tracking-[0.2em] text-slate-500">Commentaire agent</p>
                <p class="mt-1 text-sm text-slate-600">{{ dossier()!.commentaireAgent || 'Aucun commentaire.' }}</p>
              </div>
            </div>
          </div>

          <div class="rounded-[24px] border border-slate-200 bg-white p-6 shadow-sm">
            <p class="text-sm font-semibold text-blue-600">Décision</p>
            <div class="mt-4 flex flex-wrap gap-3">
              @if (peutTraiter()) {
              <button (click)="changerStatut(StatutDossier.EN_TRAITEMENT)"
                class="rounded-2xl bg-amber-500 px-4 py-2.5 text-sm font-semibold text-white transition hover:bg-amber-600">
                Prendre en charge
              </button>
              }
              @if (peutDecider()) {
              <button (click)="changerStatut(StatutDossier.VALIDE)"
                class="rounded-2xl bg-emerald-600 px-4 py-2.5 text-sm font-semibold text-white transition hover:bg-emerald-700">
                Valider
              </button>
              <button (click)="changerStatut(StatutDossier.REJETE)"
                class="rounded-2xl bg-red-600 px-4 py-2.5 text-sm font-semibold text-white transition hover:bg-red-700">
                Rejeter
              </button>
              } @else if (!peutTraiter()) {
              <p class="text-sm text-slate-500">Aucune action de traitement n’est disponible pour ce statut.</p>
              }
            </div>
            <div class="mt-5 rounded-2xl border border-slate-200 bg-slate-50 p-4 text-sm text-slate-600">
              La décision finale est enregistrée dans le journal de traitement du dossier.
            </div>
            <div class="mt-4 rounded-2xl border border-amber-200 bg-amber-50 p-4">
              <label for="demande-info" class="text-sm font-semibold text-amber-900">Demander des informations complémentaires</label>
              <textarea id="demande-info" [(ngModel)]="demandeInformation" rows="3" class="mt-2 w-full rounded-xl border border-amber-200 bg-white p-3 text-sm outline-none focus:border-amber-500" placeholder="Expliquez clairement ce qui manque au contribuable..."></textarea>
              <button (click)="demanderInformations()" class="mt-3 rounded-xl bg-amber-600 px-4 py-2 text-sm font-semibold text-white transition hover:bg-amber-700">Envoyer la demande</button>
            </div>
          </div>
        </section>

        <section class="grid gap-4 xl:grid-cols-[1fr_0.95fr]">
          <div class="rounded-[24px] border border-slate-200 bg-white p-6 shadow-sm">
            <div class="flex items-center justify-between">
              <h3 class="text-lg font-semibold text-slate-900">Pièces jointes</h3>
              <span class="rounded-full bg-slate-100 px-3 py-1 text-sm text-slate-600">
                {{ dossier()!.piecesJointes.length }} document(s)
              </span>
            </div>
            <div class="mt-5 space-y-3">
              @for (piece of dossier()!.piecesJointes; track piece.nomFichier) {
                <div class="flex items-center justify-between rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-700">
                  <div><p class="font-semibold">{{ piece.nomFichier }}</p><p class="mt-1 text-xs text-slate-500">{{ piece.type }}</p></div>
                  @if (piece.url) { <button (click)="ouvrirApercu(piece)" class="rounded-xl border border-blue-200 bg-white px-3 py-1.5 text-xs font-semibold text-blue-700">Aperçu</button> } @else { <span class="text-xs text-slate-400">Aperçu indisponible</span> }
                </div>
              } @empty {
                <p class="text-sm text-slate-500">Aucune pièce jointe.</p>
              }
            </div>
          </div>

          <div class="rounded-[24px] border border-slate-200 bg-white p-6 shadow-sm">
            <h3 class="text-lg font-semibold text-slate-900">Vérification faciale</h3>
            @if (dossier()!.resultatVerificationFaciale) {
              <div class="mt-4 rounded-2xl border border-emerald-200 bg-emerald-50 p-4">
                <p class="text-sm font-semibold text-emerald-700">
                  Score : {{ (dossier()!.resultatVerificationFaciale!.scoreSimilarite * 100).toFixed(0) }}%
                </p>
                <p class="mt-2 text-sm text-slate-600">
                  <span class="inline-flex items-center gap-1" [class.text-emerald-700]="dossier()!.resultatVerificationFaciale!.verifie" [class.text-red-700]="!dossier()!.resultatVerificationFaciale!.verifie"><app-icon [name]="dossier()!.resultatVerificationFaciale!.verifie ? 'success' : 'danger'" size="sm" />{{ dossier()!.resultatVerificationFaciale!.verifie ? 'Validé' : 'Refusé' }}</span>
                </p>
              </div>
            } @else {
              <p class="mt-3 text-sm text-slate-500">Aucune vérification enregistrée.</p>
            }
          </div>
        </section>

        <section class="rounded-[24px] border border-slate-200 bg-white p-6 shadow-sm">
          <div class="flex flex-wrap items-center justify-between gap-3"><div><h3 class="text-lg font-semibold text-slate-900">Notes internes</h3><p class="mt-1 text-sm text-slate-500">Ces notes sont visibles uniquement par les agents DGI sur cet appareil.</p></div><button (click)="enregistrerNotes()" class="rounded-xl bg-slate-900 px-4 py-2 text-sm font-semibold text-white">Enregistrer la note</button></div>
          <textarea [(ngModel)]="notesInternes" rows="4" class="mt-4 w-full rounded-2xl border border-slate-200 bg-slate-50 p-4 text-sm outline-none transition focus:border-blue-500 focus:bg-white" placeholder="Ajoutez vos observations, contrôles à effectuer ou éléments de contexte..."></textarea>
        </section>

        @if (pieceEnApercu()) {
          <div class="fixed inset-0 z-[70] flex items-center justify-center bg-slate-950/70 p-4" (click)="fermerApercu()">
            <section class="max-h-[90vh] w-full max-w-4xl overflow-auto rounded-3xl bg-white p-5 shadow-2xl" (click)="$event.stopPropagation()">
              <div class="flex items-center justify-between gap-4"><div><h3 class="text-lg font-semibold text-slate-900">{{ pieceEnApercu()!.nomFichier }}</h3><p class="text-sm text-slate-500">{{ pieceEnApercu()!.type }}</p></div><button (click)="fermerApercu()" class="rounded-xl p-2 text-xl text-slate-500 hover:bg-slate-100" aria-label="Fermer l’aperçu">✕</button></div>
              @if (apercuEstImage()) { <img [src]="pieceEnApercu()!.url" [alt]="pieceEnApercu()!.nomFichier" class="mt-5 max-h-[70vh] w-full rounded-2xl object-contain" /> } @else { <iframe [src]="urlApercu()" title="Aperçu du document" class="mt-5 h-[70vh] w-full rounded-2xl border border-slate-200"></iframe> }
              <a [href]="pieceEnApercu()!.url" target="_blank" rel="noopener" class="mt-4 inline-flex items-center gap-1 text-sm font-semibold text-blue-700 hover:underline">Ouvrir dans un nouvel onglet <app-icon name="arrow-right" size="sm" /></a>
            </section>
          </div>
        }
      } @else {
        <div class="rounded-2xl border border-slate-200 bg-white p-12 text-center text-slate-500">
          Chargement du dossier...
        </div>
      }
    </div>
  `,
})
export class DossierDetailComponent {
  private readonly dossierService = inject(DossierService);
  private readonly route = inject(ActivatedRoute);
  private readonly toastService = inject(ToastService);
  private readonly datePipe = inject(DatePipe);
  private readonly sanitizer = inject(DomSanitizer);

  readonly dossier = signal<DossierImmatriculation | null>(null);
  readonly StatutDossier = StatutDossier;
  readonly pieceEnApercu = signal<PieceJointe | null>(null);
  readonly urlApercu = signal<SafeResourceUrl | null>(null);
  notesInternes = '';
  demandeInformation = '';

  constructor() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.dossierService.obtenirParId(id).subscribe({
        next: (dossier) => {
          this.dossier.set({ ...dossier, piecesJointes: dossier.piecesJointes ?? [] });
          this.chargerNotes(dossier.id);
          this.chargerDocuments(id);
        },
        error: () => this.toastService.afficherErreur('Impossible de récupérer le dossier demandé.'),
      });
    }
  }

  /** FIX 8: Formate une date ISO en format français lisible. */
  formatDate(dateIso?: string): string {
    if (!dateIso) return '—';
    return this.datePipe.transform(dateIso, 'd MMM yyyy à HH:mm', undefined, 'fr') ?? dateIso;
  }

  getBadgeLabel(statut?: StatutDossier): string {
    switch (statut) {
      case StatutDossier.BROUILLON:     return 'Brouillon';
      case StatutDossier.SOUMIS:        return 'Soumis';
      case StatutDossier.EN_TRAITEMENT: return 'En traitement';
      case StatutDossier.VALIDE:        return 'Validé';
      case StatutDossier.REJETE:        return 'Rejeté';
      default:                          return statut ?? '—';
    }
  }

  peutTraiter(): boolean { return this.dossier()?.statut === StatutDossier.SOUMIS; }
  peutDecider(): boolean { return this.dossier()?.statut === StatutDossier.EN_TRAITEMENT; }

  changerStatut(statut: StatutDossier, commentaire = 'Traité par l’agent DGI'): void {
    const id = this.dossier()?.id;
    const dossier = this.dossier();
    const transitionAutorisee = dossier && (
      (statut === StatutDossier.EN_TRAITEMENT && this.peutTraiter())
      || ((statut === StatutDossier.VALIDE || statut === StatutDossier.REJETE) && this.peutDecider())
    );
    if (!id || !transitionAutorisee) return;

    this.dossierService.changerStatut(id, statut, commentaire).subscribe({
      next: (dossier) => {
        this.dossier.set(dossier);
        this.toastService.afficherSucces(
          statut === StatutDossier.VALIDE    ? 'Dossier validé.' :
          statut === StatutDossier.REJETE    ? 'Dossier rejeté.' :
          'Dossier pris en charge.'
        );
      },
      error: () => this.toastService.afficherErreur('La mise à jour du statut a échoué.'),
    });
  }

  ouvrirApercu(piece: PieceJointe): void {
    if (!piece.url) return;
    this.pieceEnApercu.set(piece);
    this.urlApercu.set(this.sanitizer.bypassSecurityTrustResourceUrl(piece.url));
  }

  fermerApercu(): void { this.pieceEnApercu.set(null); this.urlApercu.set(null); }
  apercuEstImage(): boolean { return /\.(png|jpe?g)$/i.test(this.pieceEnApercu()?.nomFichier ?? ''); }

  enregistrerNotes(): void {
    const id = this.dossier()?.id;
    if (!id) return;
    localStorage.setItem(`dgi-agent-note-${id}`, this.notesInternes.trim());
    this.toastService.afficherSucces('Note interne enregistrée sur cet appareil.');
  }

  demanderInformations(): void {
    const message = this.demandeInformation.trim();
    if (!message) { this.toastService.afficherErreur('Indiquez les informations demandées au contribuable.'); return; }
    this.changerStatut(StatutDossier.EN_TRAITEMENT, `Informations complémentaires demandées : ${message}`);
    this.demandeInformation = '';
  }

  private chargerDocuments(id: string): void {
    this.dossierService.obtenirDocuments(id).subscribe({
      next: (piecesJointes) => {
        const dossier = this.dossier();
        if (dossier) this.dossier.set({ ...dossier, piecesJointes });
      },
      // Le détail du dossier reste consultable si le chargement des aperçus échoue.
      error: () => this.toastService.afficherErreur('Impossible de charger les documents du dossier.'),
    });
  }

  private chargerNotes(id?: string): void { this.notesInternes = id ? localStorage.getItem(`dgi-agent-note-${id}`) ?? '' : ''; }
}
