import { Component, computed, inject, signal, ElementRef, ViewChild, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { WizardStateService } from '../../services/wizard-state.service';
import { ToastService } from '../../../../core/services/toast.service';
import { ResultatVerificationFaciale } from '../../../../core/models/resultat-verification-faciale.model';
import { FaceVerificationService } from '../../../../core/services/face-verification.service';
import { IconComponent } from '../../../../shared/components/icon/icon.component';

@Component({
  selector: 'app-step-webcam',
  standalone: true,
  imports: [CommonModule, IconComponent],
  template: `
    <div>
      <h2 class="text-2xl font-bold text-dgi-blue mb-2">Vérification faciale</h2>
      <p class="text-gray-500 mb-8">Prenez une photo en direct pour vérifier votre identité.</p>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-8">

        <!-- Colonne webcam -->
        <div class="space-y-4">
          <h3 class="flex items-center gap-2 font-semibold text-gray-700"><app-icon name="camera" size="sm" />Aperçu en direct</h3>
          <div class="relative bg-black rounded-xl overflow-hidden aspect-video">
            <video #videoElement autoplay playsinline muted
              class="w-full h-full object-cover"
              [class.hidden]="!fluxActif()">
            </video>
            @if (!fluxActif()) {
              <div class="absolute inset-0 flex flex-col items-center justify-center text-white gap-3">
                <app-icon name="camera" size="lg" />
                <p class="text-sm">Caméra non activée</p>
              </div>
            }
            @if (fluxActif()) {
              <div class="absolute inset-0 flex items-center justify-center pointer-events-none">
                <div class="w-48 h-56 border-4 border-white border-dashed rounded-full opacity-60"></div>
              </div>
            }
          </div>
          <canvas #canvasElement class="hidden"></canvas>
          <div class="flex gap-3">
            @if (!fluxActif()) {
              <button (click)="activerCamera()" class="btn-primary flex flex-1 items-center justify-center gap-2"><app-icon name="camera" size="sm" />Activer la caméra</button>
            } @else {
              <button (click)="capturerPhoto()" class="btn-primary flex flex-1 items-center justify-center gap-2"><app-icon name="camera" size="sm" />Capturer</button>
              <button (click)="arreterCamera()" class="btn-secondary inline-flex items-center gap-2"><app-icon name="close" size="sm" />Arrêter</button>
            }
          </div>
        </div>

        <!-- Colonne résultat -->
        <div class="space-y-4">
          <h3 class="flex items-center gap-2 font-semibold text-gray-700"><app-icon name="image" size="sm" />Photo capturée</h3>
          <div class="bg-gray-100 rounded-xl overflow-hidden aspect-video flex items-center justify-center">
            @if (wizardState.photoCapturee()) {
              <img [src]="wizardState.photoCapturee()!" alt="Photo capturée" class="w-full h-full object-cover" />
            } @else {
              <div class="text-center text-gray-400">
                <app-icon name="image" size="lg" />
                <p class="text-sm mt-2">Aucune photo capturée</p>
              </div>
            }
          </div>

          @if (verificationEnCours()) {
            <div class="bg-blue-50 border border-blue-200 rounded-xl p-4 flex items-center gap-3">
              <span class="w-6 h-6 border-2 border-dgi-blue border-t-transparent rounded-full animate-spin"></span>
              <p class="text-sm font-medium text-dgi-blue">Vérification en cours...</p>
            </div>
          }

          @if (wizardState.resultatFacial()) {
            <div class="rounded-xl p-4 border"
              [ngClass]="wizardState.resultatFacial()!.verifie ? 'bg-green-50 border-green-200' : 'bg-red-50 border-red-200'">
              <div class="flex items-center gap-3 mb-3">
                <span [ngClass]="wizardState.resultatFacial()!.verifie ? 'text-green-600' : 'text-red-600'"><app-icon [name]="wizardState.resultatFacial()!.verifie ? 'success' : 'danger'" size="lg" /></span>
                <p class="font-bold"
                  [ngClass]="wizardState.resultatFacial()!.verifie ? 'text-green-700' : 'text-red-700'">
                  {{ wizardState.resultatFacial()!.verifie ? 'Identité vérifiée' : 'Vérification échouée' }}
                </p>
              </div>
              <div class="mt-2">
                <div class="flex justify-between text-xs text-gray-600 mb-1">
                  <span>Score de similarité</span>
                  <span>{{ (wizardState.resultatFacial()!.scoreSimilarite * 100).toFixed(0) }}%</span>
                </div>
                <div class="w-full bg-gray-200 rounded-full h-2">
                  <div class="h-2 rounded-full transition-all duration-500"
                    [ngClass]="wizardState.resultatFacial()!.verifie ? 'bg-green-500' : 'bg-red-500'"
                    [style.width.%]="wizardState.resultatFacial()!.scoreSimilarite * 100">
                  </div>
                </div>
              </div>
              @if (!wizardState.resultatFacial()!.verifie) {
                <button (click)="recommencer()" class="mt-3 text-sm text-red-600 underline">
                  Reprendre une photo
                </button>
              }
            </div>
          }
        </div>
      </div>

      <!-- Navigation -->
      <div class="flex justify-between mt-8 pt-6 border-t border-gray-100">
        <button (click)="arreterEtPrecedent()" class="btn-secondary">← Précédent</button>
        <button (click)="wizardState.etapeSuivante()" [disabled]="!peutContinuer()"
          class="btn-primary disabled:opacity-50">
          Suivant →
        </button>
      </div>
    </div>
  `,
  styles: [`
    .btn-primary { @apply bg-dgi-blue hover:bg-dgi-blue-light text-white font-semibold px-8 py-3 rounded-lg transition-colors; }
    .btn-secondary { @apply bg-gray-100 hover:bg-gray-200 text-gray-700 font-semibold px-8 py-3 rounded-lg transition-colors; }
  `]
})
export class StepWebcamComponent implements OnDestroy {
  @ViewChild('videoElement') videoRef!: ElementRef<HTMLVideoElement>;
  @ViewChild('canvasElement') canvasRef!: ElementRef<HTMLCanvasElement>;

  readonly wizardState = inject(WizardStateService);
  private readonly faceVerificationService = inject(FaceVerificationService);
  private readonly toast = inject(ToastService);

  readonly fluxActif = signal(false);
  readonly verificationEnCours = signal(false);
  readonly verificationRequise = computed(() =>
    this.wizardState.formulaire.get('informationsGenerales.typeContribuable')?.value === 'PERSONNE_PHYSIQUE'
  );
  private fluxMedia: MediaStream | null = null;

  async activerCamera(): Promise<void> {
    try {
      this.fluxMedia = await navigator.mediaDevices.getUserMedia({
        video: { width: 640, height: 480, facingMode: 'user' }
      });
      this.videoRef.nativeElement.srcObject = this.fluxMedia;
      this.fluxActif.set(true);
    } catch {
      this.toast.afficherErreur("Impossible d'accéder à la caméra. Vérifiez les permissions.");
    }
  }

  capturerPhoto(): void {
    const video = this.videoRef.nativeElement;
    const canvas = this.canvasRef.nativeElement;
    canvas.width = video.videoWidth;
    canvas.height = video.videoHeight;
    canvas.getContext('2d')?.drawImage(video, 0, 0);
    const photoBase64 = canvas.toDataURL('image/jpeg', 0.9);
    this.wizardState.photoCapturee.set(photoBase64);
    this.arreterCamera();
    this.envoyerPourVerification(photoBase64);
  }

  private envoyerPourVerification(photoBase64: string): void {
    // The dossier and its identity document are created only at final submission.
    // The actual request is therefore made by StepRecapitulatif after upload.
    if (!this.verificationRequise()) return;
    this.verificationEnCours.set(true);
    this.wizardState.resultatFacial.set(null);
    const blob = this.base64VersBlob(photoBase64);
    const dossierId = this.wizardState.dossierId();
    const pieceReferenceId = this.wizardState.piecesJointes().find(piece => piece.id)?.id;

    if (!dossierId || !pieceReferenceId) {
      this.verificationEnCours.set(false);
      this.toast.afficherInfo('La vérification faciale sera déclenchée lors de la soumission finale, après création du dossier et upload des pièces.');
      return;
    }

    this.faceVerificationService.verifier(dossierId, pieceReferenceId, blob).subscribe({
      next: (resultat: ResultatVerificationFaciale) => {
        this.wizardState.resultatFacial.set(resultat);
        this.verificationEnCours.set(false);
        if (resultat.verifie) {
          this.toast.afficherSucces('Identité vérifiée avec succès !');
        } else {
          this.toast.afficherErreur('La vérification a échoué. Reprenez une photo.');
        }
      },
      error: () => {
        this.verificationEnCours.set(false);
        this.toast.afficherErreur('Erreur lors de la vérification faciale.');
      }
    });
  }

  recommencer(): void {
    this.wizardState.photoCapturee.set(null);
    this.wizardState.resultatFacial.set(null);
    this.activerCamera();
  }

  arreterCamera(): void {
    this.fluxMedia?.getTracks().forEach(t => t.stop());
    this.fluxMedia = null;
    this.fluxActif.set(false);
  }

  arreterEtPrecedent(): void {
    this.arreterCamera();
    this.wizardState.etapePrecedente();
  }

  peutContinuer(): boolean {
    return !this.verificationRequise() || !!this.wizardState.photoCapturee();
  }

  private base64VersBlob(base64: string): Blob {
    const [, data] = base64.split(',');
    const bytes = atob(data);
    const arr = new Uint8Array(bytes.length);
    for (let i = 0; i < bytes.length; i++) arr[i] = bytes.charCodeAt(i);
    return new Blob([arr], { type: 'image/jpeg' });
  }

  ngOnDestroy(): void {
    this.arreterCamera();
  }
}
