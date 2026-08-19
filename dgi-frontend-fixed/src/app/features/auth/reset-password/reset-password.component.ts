import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { IconComponent } from '../../../shared/components/icon/icon.component';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule, IconComponent],
  template: `
    <div class="min-h-screen bg-[radial-gradient(circle_at_top_left,_rgba(37,99,235,0.16),_transparent_40%),linear-gradient(135deg,_#f8fbff_0%,_#eef4ff_100%)] px-4 py-6 sm:px-6 lg:px-8">
      <main class="mx-auto flex min-h-[calc(100vh-3rem)] max-w-xl items-center justify-center rounded-[32px] border border-slate-200 bg-white/85 px-6 py-10 shadow-[0_30px_80px_rgba(15,23,42,0.12)] backdrop-blur sm:px-10">
        <section class="w-full max-w-md">
          <div class="text-center">
            <div class="mx-auto flex h-16 w-16 items-center justify-center rounded-2xl bg-blue-100 text-blue-700"><app-icon name="lock" size="lg" /></div>
            <h1 class="mt-5 text-2xl font-semibold text-slate-900">Choisissez un nouveau mot de passe</h1>
            <p class="mt-2 text-sm leading-6 text-slate-500">Utilisez au moins 8 caractères et conservez votre mot de passe en lieu sûr.</p>
          </div>

          @if (!token) {
            <div class="mt-7 rounded-2xl border border-amber-200 bg-amber-50 p-4 text-sm leading-6 text-amber-800">Ce lien de réinitialisation est incomplet ou invalide. Demandez un nouveau lien pour continuer.</div>
            <a routerLink="/auth/forgot-password" class="mt-5 flex w-full justify-center rounded-2xl bg-blue-600 px-4 py-3 text-sm font-semibold text-white">Demander un nouveau lien</a>
          } @else {
            <form [formGroup]="form" (ngSubmit)="reinitialiser()" class="mt-7 space-y-5">
              <div>
                <label for="password" class="mb-2 block text-sm font-semibold text-slate-700">Nouveau mot de passe</label>
                <input id="password" type="password" formControlName="motDePasse" autocomplete="new-password" class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition focus:border-blue-500 focus:bg-white focus:ring-2 focus:ring-blue-200" placeholder="Au moins 8 caractères" />
              </div>
              <div>
                <label for="confirm-password" class="mb-2 block text-sm font-semibold text-slate-700">Confirmer le mot de passe</label>
                <input id="confirm-password" type="password" formControlName="confirmation" autocomplete="new-password" class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition focus:border-blue-500 focus:bg-white focus:ring-2 focus:ring-blue-200" placeholder="Répétez le mot de passe" />
                @if (form.touched && (!form.controls.motDePasse.valid || !motsDePasseIdentiques())) { <p class="mt-2 text-xs text-rose-600">Les mots de passe doivent être identiques et contenir au moins 8 caractères.</p> }
              </div>
              <button type="submit" [disabled]="chargement()" class="flex w-full items-center justify-center rounded-2xl bg-gradient-to-r from-blue-600 to-blue-500 px-4 py-3 text-sm font-semibold text-white shadow-lg shadow-blue-200 transition hover:opacity-95 disabled:cursor-not-allowed disabled:opacity-70">
                @if (chargement()) { <span class="flex items-center gap-2"><span class="h-4 w-4 animate-spin rounded-full border-2 border-white border-t-transparent"></span>Mise à jour...</span> } @else { Mettre à jour le mot de passe }
              </button>
            </form>
          }
        </section>
      </main>
    </div>
  `,
})
export class ResetPasswordComponent {
  private readonly authService = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);
  private readonly fb = inject(FormBuilder);

  readonly chargement = signal(false);
  readonly token = this.route.snapshot.queryParamMap.get('token') ?? '';
  readonly form = this.fb.nonNullable.group({
    motDePasse: ['', [Validators.required, Validators.minLength(8)]],
    confirmation: ['', Validators.required],
  });

  motsDePasseIdentiques(): boolean {
    const { motDePasse, confirmation } = this.form.getRawValue();
    return motDePasse === confirmation;
  }

  reinitialiser(): void {
    if (this.form.invalid || !this.motsDePasseIdentiques()) {
      this.form.markAllAsTouched();
      return;
    }

    this.chargement.set(true);
    this.authService.reinitialiserMotDePasse({ token: this.token, motDePasse: this.form.controls.motDePasse.value }).subscribe({
      next: () => {
        this.chargement.set(false);
        this.toast.afficherSucces('Votre mot de passe a été mis à jour. Vous pouvez vous connecter.');
        this.router.navigate(['/auth/login']);
      },
      error: () => {
        this.chargement.set(false);
        this.toast.afficherErreur('Ce lien est invalide ou a expiré. Demandez un nouveau lien de réinitialisation.');
      },
    });
  }
}
