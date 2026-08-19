import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { IconComponent } from '../../../shared/components/icon/icon.component';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule, IconComponent],
  template: `
    <div class="min-h-screen bg-[radial-gradient(circle_at_top_left,_rgba(37,99,235,0.16),_transparent_40%),linear-gradient(135deg,_#f8fbff_0%,_#eef4ff_100%)] px-4 py-6 sm:px-6 lg:px-8">
      <main class="mx-auto flex min-h-[calc(100vh-3rem)] max-w-xl items-center justify-center rounded-[32px] border border-slate-200 bg-white/85 px-6 py-10 shadow-[0_30px_80px_rgba(15,23,42,0.12)] backdrop-blur sm:px-10">
        <section class="w-full max-w-md">
          <a routerLink="/auth/login" class="inline-flex items-center gap-2 text-sm font-semibold text-blue-600 transition hover:text-blue-700">← Retour à la connexion</a>
          <div class="mt-8 text-center">
            <div class="mx-auto flex h-16 w-16 items-center justify-center rounded-2xl bg-blue-100 text-blue-700"><app-icon name="mail" size="lg" /></div>
            <h1 class="mt-5 text-2xl font-semibold text-slate-900">Réinitialiser votre mot de passe</h1>
            <p class="mt-2 text-sm leading-6 text-slate-500">Indiquez l’adresse e-mail liée à votre compte. Nous vous enverrons un lien sécurisé pour choisir un nouveau mot de passe.</p>
          </div>

          @if (emailEnvoye()) {
            <div class="mt-7 rounded-2xl border border-emerald-200 bg-emerald-50 p-4 text-sm leading-6 text-emerald-800">
              Si cette adresse correspond à un compte, un e-mail de réinitialisation vient d’être envoyé. Vérifiez aussi vos courriers indésirables.
            </div>
          } @else {
            <form [formGroup]="form" (ngSubmit)="envoyerLien()" class="mt-7 space-y-5">
              <div>
                <label for="email" class="mb-2 block text-sm font-semibold text-slate-700">Adresse e-mail</label>
                <input id="email" type="email" formControlName="email" autocomplete="email" class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition focus:border-blue-500 focus:bg-white focus:ring-2 focus:ring-blue-200" placeholder="votre@email.com" />
                @if (form.controls.email.touched && form.controls.email.invalid) {
                  <p class="mt-2 text-xs text-rose-600">Saisissez une adresse e-mail valide.</p>
                }
              </div>
              <button type="submit" [disabled]="chargement()" class="flex w-full items-center justify-center rounded-2xl bg-gradient-to-r from-blue-600 to-blue-500 px-4 py-3 text-sm font-semibold text-white shadow-lg shadow-blue-200 transition hover:opacity-95 disabled:cursor-not-allowed disabled:opacity-70">
                @if (chargement()) { <span class="flex items-center gap-2"><span class="h-4 w-4 animate-spin rounded-full border-2 border-white border-t-transparent"></span>Envoi en cours...</span> } @else { Envoyer le lien sécurisé }
              </button>
            </form>
          }
        </section>
      </main>
    </div>
  `,
})
export class ForgotPasswordComponent {
  private readonly authService = inject(AuthService);
  private readonly fb = inject(FormBuilder);

  readonly chargement = signal(false);
  readonly emailEnvoye = signal(false);
  readonly form = this.fb.nonNullable.group({ email: ['', [Validators.required, Validators.email]] });

  envoyerLien(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.chargement.set(true);
    this.authService.demanderReinitialisationMotDePasse(this.form.getRawValue()).subscribe({
      next: () => this.terminerDemande(),
      // The same confirmation is intentionally shown when the address is unknown.
      // It prevents an attacker from discovering which e-mails have an account.
      error: () => this.terminerDemande(),
    });
  }

  private terminerDemande(): void {
    this.chargement.set(false);
    this.emailEnvoye.set(true);
  }
}
