import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { ToastService } from '../../core/services/toast.service';

interface PreferencesNotification { email: boolean; suiviDossier: boolean; informationsDgi: boolean; }

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <section class="mx-auto max-w-4xl space-y-6">
      <header class="rounded-[28px] bg-gradient-to-r from-slate-950 to-blue-800 p-6 text-white sm:flex sm:items-end sm:justify-between"><div><p class="text-xs font-bold uppercase tracking-[0.2em] text-blue-200">Mon compte</p><h1 class="mt-2 text-3xl font-semibold">Profil et paramètres</h1><p class="mt-2 text-sm text-blue-100">Gérez vos coordonnées, préférences et sécurité.</p></div><div class="mt-4 rounded-2xl bg-white/10 px-4 py-3 text-sm sm:mt-0"><p class="text-blue-100">Compte connecté</p><p class="font-semibold">{{ utilisateur()?.email }}</p></div></header>

      <div class="grid gap-6 lg:grid-cols-[1.25fr_0.75fr]">
        <form [formGroup]="form" (ngSubmit)="enregistrerProfil()" class="rounded-[28px] border border-slate-200 bg-white p-6 shadow-sm">
          <div class="flex items-center justify-between"><div><h2 class="text-xl font-semibold text-slate-900">Informations personnelles</h2><p class="mt-1 text-sm text-slate-500">Vos coordonnées de contact.</p></div><span class="rounded-full bg-blue-50 px-3 py-1 text-xs font-semibold text-blue-700">{{ utilisateur()?.role }}</span></div>
          <div class="mt-6 grid gap-4 sm:grid-cols-2"><div><label for="prenom" class="mb-2 block text-sm font-semibold text-slate-700">Prénom</label><input id="prenom" formControlName="prenom" autocomplete="given-name" class="input" /></div><div><label for="nom" class="mb-2 block text-sm font-semibold text-slate-700">Nom</label><input id="nom" formControlName="nom" autocomplete="family-name" class="input" /></div><div class="sm:col-span-2"><label for="email" class="mb-2 block text-sm font-semibold text-slate-700">Adresse e-mail</label><input id="email" type="email" formControlName="email" autocomplete="email" class="input" />@if (form.controls.email.touched && form.controls.email.invalid) { <p class="mt-1 text-xs text-rose-600">Saisissez une adresse e-mail valide.</p> }</div></div>
          <button type="submit" [disabled]="form.invalid" class="mt-6 rounded-2xl bg-blue-600 px-5 py-3 text-sm font-semibold text-white transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-60">Enregistrer les modifications</button>
        </form>

        <aside class="space-y-5"><article class="rounded-[28px] border border-slate-200 bg-white p-6 shadow-sm"><p class="text-sm font-semibold text-blue-700">Sécurité</p><h2 class="mt-2 text-xl font-semibold text-slate-900">Mot de passe</h2><p class="mt-2 text-sm leading-6 text-slate-500">Utilisez un mot de passe unique d’au moins 8 caractères.</p><a routerLink="/auth/forgot-password" class="mt-4 inline-flex rounded-xl border border-blue-200 px-4 py-2.5 text-sm font-semibold text-blue-700 transition hover:bg-blue-50">Changer mon mot de passe</a></article><article class="rounded-[28px] border border-slate-200 bg-white p-6 shadow-sm"><p class="text-sm font-semibold text-blue-700">Session</p><h2 class="mt-2 text-xl font-semibold text-slate-900">Accès sécurisé</h2><p class="mt-2 text-sm leading-6 text-slate-500">Votre session est active sur cet appareil. Déconnectez-vous après utilisation d’un poste partagé.</p></article></aside>
      </div>

      <form [formGroup]="preferencesForm" (ngSubmit)="enregistrerPreferences()" class="rounded-[28px] border border-slate-200 bg-white p-6 shadow-sm"><div class="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between"><div><h2 class="text-xl font-semibold text-slate-900">Préférences de notification</h2><p class="mt-1 text-sm text-slate-500">Choisissez les messages que vous souhaitez recevoir.</p></div><button type="submit" class="mt-2 rounded-xl bg-slate-900 px-4 py-2.5 text-sm font-semibold text-white sm:mt-0">Enregistrer</button></div><div class="mt-5 grid gap-3 md:grid-cols-3">@for (option of optionsNotifications; track option.control) { <label class="flex cursor-pointer items-start gap-3 rounded-2xl border border-slate-200 p-4 transition hover:border-blue-300"><input type="checkbox" [formControlName]="option.control" class="mt-1 h-4 w-4 rounded text-blue-600" /><span><span class="block text-sm font-semibold text-slate-800">{{ option.titre }}</span><span class="mt-1 block text-xs leading-5 text-slate-500">{{ option.description }}</span></span></label> }</div></form>
    </section>
  `,
  styles: [`.input { @apply w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition focus:border-blue-500 focus:bg-white focus:ring-2 focus:ring-blue-100; }`],
})
export class ProfileComponent {
  private readonly authService = inject(AuthService);
  private readonly toast = inject(ToastService);
  private readonly fb = inject(FormBuilder);
  readonly utilisateur = this.authService.utilisateurCourant;
  readonly form = this.fb.nonNullable.group({ nom: [this.utilisateur()?.nom ?? '', Validators.required], prenom: [this.utilisateur()?.prenom ?? '', Validators.required], email: [this.utilisateur()?.email ?? '', [Validators.required, Validators.email]] });
  readonly preferencesForm = this.fb.nonNullable.group(this.lirePreferences());
  readonly optionsNotifications = [
    { control: 'email', titre: 'E-mails importants', description: 'Décisions et actions nécessaires.' },
    { control: 'suiviDossier', titre: 'Suivi des dossiers', description: 'Changements de statut et demandes DGI.' },
    { control: 'informationsDgi', titre: 'Informations DGI', description: 'Nouveautés et informations de service.' },
  ];

  enregistrerProfil(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.authService.mettreAJourProfilLocal(this.form.getRawValue());
    this.toast.afficherSucces('Vos informations de profil ont été enregistrées.');
  }

  enregistrerPreferences(): void { localStorage.setItem('dgi-preferences-notifications', JSON.stringify(this.preferencesForm.getRawValue())); this.toast.afficherSucces('Vos préférences de notification ont été enregistrées.'); }
  private lirePreferences(): PreferencesNotification { try { return { email: true, suiviDossier: true, informationsDgi: false, ...(JSON.parse(localStorage.getItem('dgi-preferences-notifications') ?? '{}') as Partial<PreferencesNotification>) }; } catch { return { email: true, suiviDossier: true, informationsDgi: false }; } }
}
