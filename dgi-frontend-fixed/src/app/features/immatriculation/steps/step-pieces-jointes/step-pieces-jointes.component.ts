import { Component, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { WizardStateService } from '../../services/wizard-state.service';
import { ToastService } from '../../../../core/services/toast.service';
import { PieceJointe, TypePieceJointe } from '../../../../core/models/piece-jointe.model';
import { OcrService } from '../../../../core/services/ocr.service';
import { IconComponent } from '../../../../shared/components/icon/icon.component';

@Component({
  selector: 'app-step-pieces-jointes',
  standalone: true,
  imports: [CommonModule, IconComponent],
  template: `
    <div class="space-y-6">

      <!-- ── En-tête ── -->
      <div class="rounded-2xl border border-blue-100 bg-blue-50/70 p-4">
        <h2 class="text-2xl font-semibold text-slate-900">Pièces jointes</h2>
        <p class="mt-2 text-sm text-slate-600">
          Téléversez votre pièce d'identité : les informations (Nom, Prénom, CIN)
          sont extraites automatiquement depuis le texte arabe pour accélérer votre demande.
        </p>
      </div>

      <!-- ── Checklist documents requis ── -->
      <div class="rounded-[24px] border border-slate-200 bg-white p-5 shadow-sm">
        <div class="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h3 class="text-base font-semibold text-slate-900">Vérification des documents</h3>
            <p class="mt-1 text-sm text-slate-500">Ajoutez la pièce requise avant de continuer.</p>
          </div>
          <span
            class="rounded-full px-3 py-1 text-xs font-semibold"
            [ngClass]="documentsManquants().length
              ? 'bg-amber-100 text-amber-800'
              : 'bg-emerald-100 text-emerald-800'">
            {{ documentsManquants().length
              ? documentsManquants().length + ' document(s) requis'
              : 'Dossier documentaire complet' }}
          </span>
        </div>
        <ul class="mt-4 grid gap-2 sm:grid-cols-2">
          @for (doc of documentsRequis(); track doc.type) {
            <li
              class="flex items-center gap-2 rounded-xl px-3 py-2 text-sm"
              [ngClass]="doc.ajoute
                ? 'bg-emerald-50 text-emerald-800'
                : 'bg-slate-50 text-slate-600'">
              <span>{{ doc.ajoute ? '✓' : '○' }}</span>
              {{ doc.libelle }}
            </li>
          }
        </ul>
      </div>

      <!-- ── Zone d'upload ── -->
      <div class="rounded-[24px] border border-slate-200 bg-slate-50 p-5">

        <!-- Sélecteur de type de document -->
        <div class="mb-5">
          <label class="mb-2 block text-sm font-semibold text-slate-700">Type de document</label>
          <select
            class="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
            (change)="typePieceSelectionnee = $any($event.target).value">
            <option value="CIN">Carte d'identité nationale (CIN)</option>
            <option value="PASSEPORT">Passeport</option>
            <option value="REGISTRE_COMMERCE">Registre de commerce</option>
            <option value="AUTRE">Autre document</option>
          </select>
        </div>

        <!-- Zone drag & drop -->
        <div
          class="rounded-[20px] border-2 border-dashed border-slate-300 bg-white p-8 text-center transition-all"
          [ngClass]="dragActif()
            ? 'border-blue-500 bg-blue-50'
            : 'hover:border-blue-400 hover:bg-slate-50'"
          (dragover)="onDragOver($event)"
          (dragleave)="dragActif.set(false)"
          (drop)="onDrop($event)">

          <div class="flex justify-center text-slate-400">
            <svg class="h-10 w-10" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
                d="M3 16.5v2.25A2.25 2.25 0 005.25 21h13.5A2.25 2.25 0 0021 18.75V16.5m-13.5-9L12 3m0 0l4.5 4.5M12 3v13.5" />
            </svg>
          </div>

          <p class="mt-3 text-base font-semibold text-slate-800">Glissez-déposez vos fichiers ici</p>
          <p class="mt-2 text-sm text-slate-500">ou</p>

          <label class="mt-4 inline-flex cursor-pointer rounded-2xl bg-blue-600 px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-blue-700">
            Parcourir les fichiers
            <input
              type="file"
              class="hidden"
              multiple
              accept=".pdf,.jpg,.jpeg,.png"
              (change)="onFichiersSelectionnes($event)" />
          </label>

          <p class="mt-4 text-xs text-slate-500">
            PDF, JPG, PNG uniquement — 5 Mo max par fichier.<br/>
            Pour une CIN ou un passeport, le <strong>Nom</strong>, <strong>Prénom</strong>
            et <strong>Numéro CIN</strong> sont extraits automatiquement depuis le texte arabe,
            puis vous passez à l'étape suivante.
          </p>
        </div>
      </div>

      <!-- ── Liste des documents ajoutés ── -->
      @if (wizardState.piecesJointes().length > 0) {
        <div class="space-y-3">
          <h3 class="text-base font-semibold text-slate-900">Documents ajoutés</h3>

          @for (piece of wizardState.piecesJointes(); track piece.nomFichier) {
            <div class="flex items-center justify-between rounded-[20px] border border-slate-200 bg-white p-4 shadow-sm">
              <div class="flex items-center gap-3 flex-1 min-w-0">

                <!-- Icône type -->
                <div class="flex-shrink-0 rounded-xl bg-blue-50 p-2 text-blue-600">
                  <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                      d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414A1 1 0 0121 9.414V19a2 2 0 01-2 2z"/>
                  </svg>
                </div>

                <div class="flex-1 min-w-0">
                  <p class="truncate text-sm font-semibold text-slate-800">{{ piece.nomFichier }}</p>
                  <p class="text-xs text-slate-500">{{ piece.type }} — {{ formatTaille(piece.tailleOctets) }}</p>

                  <!-- Statut OCR -->
                  @if (piece.ocr?.statut === 'EN_COURS') {
                    <p class="mt-1 flex items-center gap-1.5 text-xs font-medium text-blue-700">
                      <span class="h-3 w-3 animate-spin rounded-full border-2 border-blue-600 border-t-transparent"></span>
                      Extraction des informations arabes en cours…
                    </p>
                  }
                  @if (piece.ocr?.statut === 'TERMINE') {
                    <p class="mt-1 text-xs font-medium text-emerald-700">
                      ✓ Nom, Prénom et CIN extraits — formulaire prérempli
                    </p>
                  }
                  @if (piece.ocr?.statut === 'ERREUR') {
                    <p class="mt-1 text-xs text-amber-700">
                      ⚠ Extraction indisponible — saisissez les informations manuellement.
                    </p>
                  }
                </div>
              </div>

              <!-- Bouton supprimer -->
              <button
                type="button"
                (click)="supprimerPiece(piece)"
                class="ml-3 flex-shrink-0 rounded-lg p-2 text-red-400 transition hover:bg-red-50 hover:text-red-600"
                [title]="'Supprimer ' + piece.nomFichier">
                <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
                </svg>
              </button>
            </div>
          }
        </div>

        <!-- ── Bouton relancer OCR ── -->
        @if (pieceIdentite()) {
          <div class="flex flex-col gap-4 rounded-[24px] border border-blue-200 bg-blue-50 p-4 sm:flex-row sm:items-center sm:justify-between">
            <div>
              <p class="flex items-center gap-2 text-sm font-semibold text-blue-700">
                <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/>
                </svg>
                Extraction automatique (OCR arabe)
              </p>
              <p class="mt-1 text-sm text-slate-600">
                Extrait <strong>الاسم</strong> (Nom), <strong>اللقب</strong> (Prénom)
                et <strong>رقم بطاقة التعريف</strong> (CIN) depuis la pièce d'identité.
              </p>
            </div>
            <button
              type="button"
              (click)="lancerOcr()"
              [disabled]="ocrEnCours()"
              class="rounded-2xl bg-blue-600 px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-60">
              @if (ocrEnCours()) {
                <span class="flex items-center gap-2">
                  <span class="h-4 w-4 animate-spin rounded-full border-2 border-white border-t-transparent"></span>
                  Extraction en cours…
                </span>
              } @else {
                Réanalyser la pièce d'identité
              }
            </button>
          </div>
        }

        <!-- ── Résumé OCR réussi ── -->
        @if (pieceIdentite()?.ocr?.statut === 'TERMINE') {
          <div class="rounded-2xl border border-emerald-200 bg-emerald-50 p-4">

            <div class="flex flex-wrap items-center justify-between gap-3">
              <span class="text-sm font-semibold text-emerald-900">
                ✓ Formulaire prérempli depuis les données arabes
              </span>
              <button
                type="button"
                (click)="wizardState.allerEtape(2)"
                class="rounded-xl bg-emerald-700 px-4 py-2 text-sm font-semibold text-white transition hover:bg-emerald-800">
                Vérifier le formulaire →
              </button>
            </div>

            <!-- Données extraites affichées -->
            <dl class="mt-4 grid gap-3 border-t border-emerald-200 pt-4 sm:grid-cols-3">

              @if (pieceIdentite()?.ocr?.champs?.nom) {
                <div class="rounded-xl bg-white px-4 py-3 shadow-sm">
                  <dt class="text-xs font-semibold uppercase tracking-wide text-emerald-600">
                    الاسم — Nom
                  </dt>
                  <dd class="mt-1 text-sm font-bold text-slate-800">
                    {{ pieceIdentite()?.ocr?.champs?.nom }}
                  </dd>
                </div>
              }

              @if (pieceIdentite()?.ocr?.champs?.prenom) {
                <div class="rounded-xl bg-white px-4 py-3 shadow-sm">
                  <dt class="text-xs font-semibold uppercase tracking-wide text-emerald-600">
                    اللقب — Prénom
                  </dt>
                  <dd class="mt-1 text-sm font-bold text-slate-800">
                    {{ pieceIdentite()?.ocr?.champs?.prenom }}
                  </dd>
                </div>
              }

              @if (pieceIdentite()?.ocr?.champs?.cin) {
                <div class="rounded-xl bg-white px-4 py-3 shadow-sm">
                  <dt class="text-xs font-semibold uppercase tracking-wide text-emerald-600">
                    {{ pieceIdentite()?.type === 'PASSEPORT' ? 'رقم الجواز — Passeport' : 'رقم بطاقة التعريف — CIN' }}
                  </dt>
                  <dd class="mt-1 text-sm font-bold text-slate-800 tracking-widest">
                    {{ pieceIdentite()?.ocr?.champs?.cin }}
                  </dd>
                </div>
              }

              @if (pieceIdentite()?.ocr?.champs?.dateNaissance) {
                <div class="rounded-xl bg-white px-4 py-3 shadow-sm">
                  <dt class="text-xs font-semibold uppercase tracking-wide text-emerald-600">
                    تاريخ الولادة — Date de naissance
                  </dt>
                  <dd class="mt-1 text-sm font-bold text-slate-800">
                    {{ pieceIdentite()?.ocr?.champs?.dateNaissance }}
                    @if (ageExtrait() !== undefined) {
                      <span class="ml-2 text-xs text-slate-500">({{ ageExtrait() }} ans)</span>
                    }
                  </dd>
                </div>
              }

            </dl>

            <p class="mt-3 text-xs text-emerald-700">
              ℹ Ces informations ont été automatiquement copiées dans le formulaire de l'étape 2.
              Vous pouvez les modifier si nécessaire.
            </p>
          </div>
        }
      }

      <!-- ── Navigation bas de page ── -->
      <div class="flex flex-wrap justify-between gap-3 border-t border-slate-200 pt-6">
        <span></span>
        <div class="flex flex-col items-end gap-2">
          @if (documentsManquants().length > 0) {
            <p class="text-right text-xs font-medium text-amber-700">
              Pour continuer, ajoutez : {{ libellesDocumentsManquants() }}.
            </p>
          }
          <button
            type="button"
            (click)="continuer()"
            [disabled]="documentsManquants().length > 0 || ocrEnCours()"
            class="rounded-2xl bg-blue-600 px-6 py-3 text-sm font-semibold text-white transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-60">
            Suivant →
          </button>
        </div>
      </div>

    </div>
  `,
})
export class StepPiecesJointesComponent {

  readonly wizardState  = inject(WizardStateService);
  private readonly ocrService = inject(OcrService);
  private readonly toast      = inject(ToastService);

  readonly dragActif   = signal(false);
  readonly ocrEnCours  = signal(false);

  /** Pièce d'identité ajoutée (CIN ou passeport). */
  readonly pieceIdentite = computed(() =>
    this.wizardState.piecesJointes().find(
      (p) => p.type === 'CIN' || p.type === 'PASSEPORT'
    )
  );

  /** Âge calculé depuis la date extraite (ISO YYYY-MM-DD). */
  readonly ageExtrait = computed(() =>
    this.calculerAge(this.pieceIdentite()?.ocr?.champs?.dateNaissance)
  );

  /** Liste des documents obligatoires selon le type de contribuable. */
  readonly documentsRequis = computed(() => {
    const estPM =
      this.wizardState.formulaire.get('informationsGenerales.typeContribuable')?.value
      === 'PERSONNE_MORALE';
    const typesAjoutes = new Set(
      this.wizardState.piecesJointes().map((p) => p.type)
    );
    const liste = estPM
      ? [{ type: 'REGISTRE_COMMERCE' as TypePieceJointe, libelle: 'Registre de commerce' }]
      : [{ type: 'CIN' as TypePieceJointe, libelle: "Carte d'identité nationale ou passeport",
           alternatif: 'PASSEPORT' as TypePieceJointe }];

    return liste.map((doc) => ({
      ...doc,
      ajoute:
        typesAjoutes.has(doc.type) ||
        ('alternatif' in doc && typesAjoutes.has(doc.alternatif)),
    }));
  });

  readonly documentsManquants = computed(() =>
    this.documentsRequis().filter((d) => !d.ajoute)
  );
  readonly libellesDocumentsManquants = computed(() =>
    this.documentsManquants().map((d) => d.libelle).join(', ')
  );

  typePieceSelectionnee: TypePieceJointe = 'CIN';

  private readonly typesAcceptes = new Set([
    'application/pdf', 'image/jpeg', 'image/jpg', 'image/png',
  ]);
  private readonly extensionsAcceptees = new Set(['pdf', 'jpg', 'jpeg', 'png']);

  // ── Drag & Drop ────────────────────────────────────────────────────────────

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    this.dragActif.set(true);
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    this.dragActif.set(false);
    this.traiterFichiers(Array.from(event.dataTransfer?.files ?? []));
  }

  onFichiersSelectionnes(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.traiterFichiers(Array.from(input.files ?? []));
    input.value = '';
  }

  // ── Traitement des fichiers ────────────────────────────────────────────────

  private traiterFichiers(fichiers: File[]): void {
    fichiers.forEach((fichier) => {
      const ext = fichier.name.split('.').pop()?.toLowerCase() ?? '';

      if (!this.typesAcceptes.has(fichier.type) || !this.extensionsAcceptees.has(ext)) {
        this.toast.afficherErreur(`${fichier.name} : seul un PDF, JPG ou PNG est accepté.`);
        return;
      }
      if (fichier.size > 5 * 1024 * 1024) {
        this.toast.afficherErreur(`${fichier.name} dépasse la limite de 5 Mo.`);
        return;
      }
      if (fichier.size === 0) {
        this.toast.afficherErreur(`${fichier.name} est vide.`);
        return;
      }
      if (
        this.wizardState.piecesJointes().some(
          (p) => p.nomFichier === fichier.name && p.tailleOctets === fichier.size
        )
      ) {
        this.toast.afficherErreur(`${fichier.name} a déjà été ajouté.`);
        return;
      }

      const piece: PieceJointe = {
        type:         this.typePieceSelectionnee,
        nomFichier:   fichier.name,
        tailleOctets: fichier.size,
        fichier,
      };

      this.wizardState.piecesJointes.update((liste) => [...liste, piece]);

      // Lancer l'OCR automatiquement pour CIN et passeport
      if (piece.type === 'CIN' || piece.type === 'PASSEPORT') {
        this.analyserPiece(piece);
      }
    });
  }

  supprimerPiece(piece: PieceJointe): void {
    this.wizardState.piecesJointes.update((liste) =>
      liste.filter(
        (p) => !(p.nomFichier === piece.nomFichier && p.tailleOctets === piece.tailleOctets)
      )
    );
  }

  continuer(): void {
    if (this.documentsManquants().length) {
      this.toast.afficherErreur('Ajoutez les documents requis avant de continuer.');
      return;
    }
    this.wizardState.etapeSuivante();
  }

  lancerOcr(): void {
    const piece = this.pieceIdentite();
    if (!piece?.fichier) {
      this.toast.afficherErreur("Veuillez d'abord ajouter une pièce à analyser.");
      return;
    }
    this.analyserPiece(piece);
  }

  // ── OCR ───────────────────────────────────────────────────────────────────

  /**
   * Appel OCR :
   *   1. Marque la pièce EN_COURS
   *   2. Appelle OcrService → extrait الاسم (nom), اللقب (prénom), رقم CIN
   *   3. Pré-remplit le formulaire via wizardState.preRemplirDepuisOcr()
   *   4. Navigue automatiquement vers l'étape 2
   */
  private analyserPiece(piece: PieceJointe): void {
    if (!piece.fichier) return;

    this.ocrEnCours.set(true);
    this.mettreAJourOcr(piece, { statut: 'EN_COURS' });

    this.ocrService.extraireInfos(piece.fichier, piece.type).subscribe({
      next: (champs) => {
        // ① Stocker le résultat OCR dans la pièce
        this.mettreAJourOcr(piece, { statut: 'TERMINE', champs });

        // ② Pré-remplir le formulaire (nom, prenom, cin, dateNaissance)
        //    AVANT la navigation pour que les champs soient déjà remplis
        //    quand l'étape 2 s'affiche
        this.wizardState.preRemplirDepuisOcr(champs, piece.type);

        this.ocrEnCours.set(false);

        // ③ Naviguer vers étape 2
        this.wizardState.allerEtape(2);

        this.toast.afficherSucces(
          'Informations extraites ✓ — Nom, Prénom et CIN ont été copiés dans le formulaire.'
        );
      },
      error: (erreur: Error) => {
        this.mettreAJourOcr(piece, { statut: 'ERREUR', erreur: erreur.message });
        this.toast.afficherErreur(
          erreur.message || "L'extraction OCR a échoué. Remplissez les champs manuellement."
        );
        this.ocrEnCours.set(false);
        // Laisser l'utilisateur continuer manuellement sans bloquer
      },
    });
  }

  private mettreAJourOcr(
    piece: PieceJointe,
    ocr: NonNullable<PieceJointe['ocr']>
  ): void {
    this.wizardState.piecesJointes.update((liste) =>
      liste.map((item) =>
        item.nomFichier === piece.nomFichier && item.tailleOctets === piece.tailleOctets
          ? { ...item, ocr }
          : item
      )
    );
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  formatTaille(octets?: number): string {
    if (!octets) return '';
    return octets < 1024 * 1024
      ? `${(octets / 1024).toFixed(1)} Ko`
      : `${(octets / (1024 * 1024)).toFixed(1)} Mo`;
  }

  private calculerAge(dateNaissance?: string): number | undefined {
    if (!dateNaissance || !/^\d{4}-\d{2}-\d{2}$/.test(dateNaissance)) return undefined;
    const naissance = new Date(`${dateNaissance}T00:00:00`);
    if (Number.isNaN(naissance.getTime()) || naissance > new Date()) return undefined;
    const aujourd_hui = new Date();
    let age = aujourd_hui.getFullYear() - naissance.getFullYear();
    const anniversairePasse =
      aujourd_hui.getMonth() > naissance.getMonth() ||
      (aujourd_hui.getMonth() === naissance.getMonth() &&
        aujourd_hui.getDate() >= naissance.getDate());
    return anniversairePasse ? age : age - 1;
  }
}