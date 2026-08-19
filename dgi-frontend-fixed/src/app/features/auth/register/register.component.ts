import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService, SocialAuthProvider } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { IconComponent } from '../../../shared/components/icon/icon.component';
import { AuthFeatureIconComponent, AuthFeatureIconName, AuthFeatureIconTone } from '../auth-feature-icon.component';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule, IconComponent, AuthFeatureIconComponent],
  template: `
    <div class="animate-fade-in relative min-h-screen overflow-hidden bg-slate-50 px-4 py-5 sm:px-6 lg:px-8">
      <div class="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_5%_5%,_rgba(37,99,235,0.18),_transparent_26%),radial-gradient(circle_at_95%_90%,_rgba(14,165,233,0.14),_transparent_30%)]"></div>
      <div class="relative mx-auto flex min-h-[calc(100vh-3rem)] max-w-6xl flex-col justify-center overflow-hidden rounded-[36px] border border-slate-200/80 bg-white/85 shadow-[0_32px_100px_rgba(15,23,42,0.16)] backdrop-blur xl:flex-row">
        <section class="relative flex flex-1 flex-col justify-center overflow-hidden bg-slate-950 px-6 py-10 text-white sm:px-10 lg:px-14">
          <div class="absolute -right-24 -top-20 h-72 w-72 rounded-full bg-blue-500/20 blur-3xl"></div>
          <div class="relative animate-fade-in">
          <div class="inline-flex w-fit items-center gap-2 rounded-full border border-white/15 bg-white/10 px-3 py-1 text-sm text-slate-200">
            <app-icon name="immatriculation" size="sm" />
            Nouvelle inscription citoyenne
          </div>
          <div class="mt-8 flex items-center gap-3"><img src="/assets/portail-fiscal-logo.svg" alt="" class="h-9 w-9 rounded-xl bg-white p-1.5" /><p class="text-sm font-semibold tracking-wide text-blue-100">DIRECTION GÉNÉRALE DES IMPÔTS</p></div>
          <h1 class="mt-5 text-3xl font-semibold leading-tight sm:text-4xl">Créez votre compte en quelques secondes</h1>
          <p class="mt-4 max-w-xl text-sm leading-7 text-slate-300 sm:text-base">L’inscription sécurisée vous donne accès à tous les services en ligne de la Direction Générale des Impôts.</p>
          <div class="mt-8 grid gap-3 sm:grid-cols-3">
            @for (feature of features; track feature.title) {
              <article class="rounded-2xl border border-white/10 bg-white/[0.06] p-4 transition hover:border-white/20 hover:bg-white/[0.09]">
                <app-auth-feature-icon [icon]="feature.icon" [tone]="feature.tone" />
                <h2 class="mt-4 text-sm font-semibold text-white">{{ feature.title }}</h2>
                <p class="mt-1 text-xs leading-5 text-slate-300">{{ feature.description }}</p>
              </article>
            }
          </div>
          </div>
        </section>

        <section class="flex flex-1 items-center justify-center px-6 py-8 sm:px-10 lg:px-12">
          <div class="w-full max-w-md">
            <div class="mb-6 text-center">
              <div class="mx-auto flex h-16 w-16 items-center justify-center rounded-2xl bg-gradient-to-br from-blue-600 to-cyan-500 p-3 shadow-lg shadow-blue-200">
                <img src="/assets/portail-fiscal-logo.svg" alt="Portail Fiscal" class="h-full w-full object-contain" />
              </div>
              <h2 class="mt-5 text-2xl font-semibold text-slate-900">Créer un compte</h2>
              <p class="mt-2 text-sm text-slate-500">Accédez à vos services fiscaux en toute simplicité.</p>
            </div>

            <div class="grid grid-cols-3 gap-3">
              <button type="button" (click)="connexionSociale('google')" class="flex h-12 items-center justify-center gap-2 rounded-2xl border border-slate-200 bg-white text-sm font-semibold text-slate-700 transition hover:-translate-y-0.5 hover:border-slate-300 hover:shadow-md" aria-label="S’inscrire avec Google"><span class="text-base font-bold text-red-500">G</span><span class="hidden sm:inline">Google</span></button>
              <button type="button" (click)="connexionSociale('facebook')" class="flex h-12 items-center justify-center gap-2 rounded-2xl border border-slate-200 bg-white text-sm font-semibold text-slate-700 transition hover:-translate-y-0.5 hover:border-slate-300 hover:shadow-md" aria-label="S’inscrire avec Facebook"><span class="text-lg font-bold text-[#1877f2]">f</span><span class="hidden sm:inline">Facebook</span></button>
              <button type="button" (click)="connexionSociale('twitter')" class="flex h-12 items-center justify-center gap-2 rounded-2xl border border-slate-200 bg-white text-sm font-semibold text-slate-700 transition hover:-translate-y-0.5 hover:border-slate-300 hover:shadow-md" aria-label="S’inscrire avec X, anciennement Twitter"><span class="text-base font-bold text-slate-950">𝕏</span><span class="hidden sm:inline">X</span></button>
            </div>
            <div class="my-6 flex items-center gap-3 text-xs font-medium uppercase tracking-[0.16em] text-slate-400"><span class="h-px flex-1 bg-slate-200"></span><span>ou avec votre e-mail</span><span class="h-px flex-1 bg-slate-200"></span></div>

            <form [formGroup]="form" (ngSubmit)="inscription()" class="space-y-4" novalidate>
              <div class="grid gap-4 sm:grid-cols-2">
                <div>
                  <label class="mb-2 block text-sm font-semibold text-slate-700">Nom</label>
                  <input type="text" autocomplete="family-name" formControlName="nom" class="h-14 w-full rounded-xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-blue-500 focus:bg-white focus:ring-4 focus:ring-blue-100" placeholder="Votre nom" />
                </div>
                <div>
                  <label class="mb-2 block text-sm font-semibold text-slate-700">Prénom</label>
                  <input type="text" autocomplete="given-name" formControlName="prenom" class="h-14 w-full rounded-xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-blue-500 focus:bg-white focus:ring-4 focus:ring-blue-100" placeholder="Votre prénom" />
                </div>
              </div>
              <div>
                <label class="mb-2 block text-sm font-semibold text-slate-700">Adresse e-mail</label>
                <input type="email" autocomplete="email" formControlName="email" class="h-14 w-full rounded-xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-blue-500 focus:bg-white focus:ring-4 focus:ring-blue-100" placeholder="votre@email.com" />
              </div>
              <div>
                <label class="mb-2 block text-sm font-semibold text-slate-700">Mot de passe</label>
                <input type="password" autocomplete="new-password" formControlName="motDePasse" class="h-14 w-full rounded-xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-blue-500 focus:bg-white focus:ring-4 focus:ring-blue-100" placeholder="••••••••" />
              </div>
              <div>
                <label class="mb-2 block text-sm font-semibold text-slate-700">Confirmer le mot de passe</label>
                <input type="password" autocomplete="new-password" formControlName="confirmationMotDePasse" class="h-14 w-full rounded-xl border border-slate-200 bg-slate-50 px-4 text-sm outline-none transition focus:border-blue-500 focus:bg-white focus:ring-4 focus:ring-blue-100" placeholder="••••••••" />
              </div>

              <button type="submit" [disabled]="chargement()" class="flex h-14 w-full items-center justify-center rounded-xl bg-[#2563eb] px-4 text-sm font-semibold text-white shadow-lg shadow-blue-200 transition hover:-translate-y-0.5 hover:bg-blue-700 hover:shadow-xl disabled:cursor-not-allowed disabled:opacity-70">
                @if (chargement()) {
                  <span class="flex items-center gap-2"><span class="h-4 w-4 animate-spin rounded-full border-2 border-white border-t-transparent"></span>Inscription...</span>
                } @else {
                  Créer mon compte
                }
              </button>
            </form>

            <p class="mt-6 text-center text-sm text-slate-500">
              Déjà un compte ?
              <a routerLink="/auth/login" class="font-semibold text-blue-600 hover:underline">Se connecter</a>
            </p>
            <div class="mt-6 flex items-center justify-center gap-2 text-center text-xs leading-5 text-slate-500"><span aria-hidden="true">🔒</span><span>Vos informations sont protégées et sécurisées.</span></div>
          </div>
        </section>
      </div>
    </div>
  `,
})
export class RegisterComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);
  private readonly fb = inject(FormBuilder);

  readonly chargement = signal(false);
  readonly features: ReadonlyArray<{ title: string; description: string; icon: AuthFeatureIconName; tone: AuthFeatureIconTone }> = [
    { title: 'Compte personnel', description: 'Créez votre accès en quelques étapes.', icon: 'user-plus', tone: 'purple' },
    { title: 'Données protégées', description: 'Vos informations restent sécurisées.', icon: 'shield-check', tone: 'cyan' },
    { title: 'Suivi simplifié', description: 'Restez informé de chaque évolution.', icon: 'bell', tone: 'green' },
  ];
  readonly form = this.fb.group({
    nom: ['', Validators.required],
    prenom: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    motDePasse: ['', [Validators.required, Validators.minLength(8)]],
    confirmationMotDePasse: [''],
  });

  inscription(): void {
    if (this.form.invalid) return;
    this.chargement.set(true);
    const { nom, prenom, email, motDePasse } = this.form.value;
    this.authService.register({ nom: nom!, prenom: prenom!, email: email!, motDePasse: motDePasse! }).subscribe({
      next: () => {
        this.chargement.set(false);
        this.router.navigate(['/immatriculation']);
      },
      error: (_err: unknown) => {
        this.chargement.set(false);
        this.toast.afficherErreur('Erreur lors de l\'inscription.');
      }
    });
  }

  connexionSociale(provider: SocialAuthProvider): void {
    this.authService.demarrerConnexionSociale(provider);
  }
}
