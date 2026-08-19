import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { IconComponent } from '../../shared/components/icon/icon.component';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink, IconComponent],
  template: `
    <main class="relative overflow-hidden bg-slate-950 text-white">
      <div class="pointer-events-none absolute -right-24 top-16 h-64 w-64 rounded-full bg-blue-500/20 blur-3xl"></div>
      <div class="pointer-events-none absolute left-8 top-36 h-40 w-40 rounded-full bg-cyan-400/20 blur-3xl"></div>

      <section class="relative mx-auto flex min-h-screen max-w-7xl flex-col justify-center px-6 py-16 lg:px-12">
        <div class="grid gap-12 lg:grid-cols-[1.1fr_0.9fr] lg:items-center">
          <div class="space-y-8">
            <span class="inline-flex rounded-full border border-slate-700 bg-slate-900/80 px-4 py-2 text-xs uppercase tracking-[0.3em] text-cyan-300 shadow-sm">Portail fiscal</span>
            <h1 class="max-w-3xl text-4xl font-semibold tracking-tight text-white sm:text-5xl lg:text-6xl">Gérez vos immatriculations fiscales avec clarté, rapidité et design moderne.</h1>
            <p class="max-w-2xl text-base leading-8 text-slate-300 sm:text-lg">Une interface pensée pour les contribuables et les agents DGI, avec un parcours simplifié, des notifications intelligentes et un tableau de bord sécurisé.</p>
            <div class="flex flex-col gap-4 sm:flex-row">
              <a routerLink="/auth/login" class="inline-flex items-center justify-center rounded-full bg-blue-500 px-6 py-3 text-sm font-semibold text-white shadow-lg transition hover:bg-blue-400">Se connecter</a>
              <a routerLink="/auth/register" class="inline-flex items-center justify-center rounded-full border border-slate-700 bg-slate-900/80 px-6 py-3 text-sm font-semibold text-slate-100 transition hover:bg-slate-800">Créer un compte</a>
            </div>
          </div>

          <div class="relative overflow-hidden rounded-[32px] border border-white/10 bg-slate-900/80 p-6 shadow-2xl backdrop-blur">
            <div class="absolute -left-12 top-10 h-32 w-32 rounded-full bg-cyan-300/10 blur-3xl"></div>
            <div class="absolute -right-10 bottom-10 h-32 w-32 rounded-full bg-blue-500/10 blur-3xl"></div>
            <div class="relative rounded-[26px] bg-slate-950 p-5">
              <div class="mb-5 flex items-center justify-between gap-4">
                <div>
                  <p class="text-xs uppercase tracking-[0.3em] text-slate-500">Statut fiscal</p>
                  <h2 class="mt-2 text-2xl font-semibold text-white">Dashboard agent & contribuable</h2>
                </div>
                <div class="rounded-2xl bg-slate-800 px-3 py-2 text-xs text-slate-300">En direct</div>
              </div>
              <div class="relative overflow-hidden rounded-[24px] bg-gradient-to-br from-slate-800 via-slate-900 to-slate-950 p-4">
                <img src="assets/hero-immatriculation.svg" alt="Interface fiscale" class="h-64 w-full rounded-[22px] object-cover shadow-2xl" />
                <div class="absolute inset-x-6 bottom-6 rounded-3xl border border-white/10 bg-slate-950/80 p-4 backdrop-blur">
                  <div class="mb-3 flex items-center justify-between gap-3">
                    <div>
                      <p class="text-xs uppercase tracking-[0.3em] text-slate-400">Prochaine action</p>
                      <p class="text-sm font-semibold text-white">Validation du dossier n°1342</p>
                    </div>
                    <span class="rounded-full bg-emerald-500/15 px-3 py-1 text-xs font-semibold text-emerald-300">Statut actif</span>
                  </div>
                  <div class="grid gap-3 sm:grid-cols-3">
                    <div class="rounded-2xl bg-slate-900/90 px-4 py-3 text-xs text-slate-400">
                      <p class="font-semibold text-white">12</p>
                      <p>Documents</p>
                    </div>
                    <div class="rounded-2xl bg-slate-900/90 px-4 py-3 text-xs text-slate-400">
                      <p class="font-semibold text-white">3 min</p>
                      <p>Temps estimé</p>
                    </div>
                    <div class="rounded-2xl bg-slate-900/90 px-4 py-3 text-xs text-slate-400">
                      <p class="font-semibold text-white">5</p>
                      <p>Nouvelles demandes</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <section class="mt-16 grid gap-6 md:grid-cols-3">
          <article class="group rounded-[28px] border border-white/10 bg-slate-900/80 p-6 transition duration-500 hover:-translate-y-2 hover:border-blue-500/20 hover:bg-slate-900">
            <div class="mb-5 inline-flex h-12 w-12 items-center justify-center rounded-3xl bg-blue-500/10 text-blue-300 transition group-hover:bg-blue-500/20">
              <app-icon name="documents" size="lg" />
            </div>
            <h3 class="text-xl font-semibold text-white">Démarches simplifiées</h3>
            <p class="mt-3 text-sm leading-7 text-slate-400">Suivez chaque étape de l’immatriculation avec des formulaires intelligents et des validations instantanées.</p>
          </article>

          <article class="group rounded-[28px] border border-white/10 bg-slate-900/80 p-6 transition duration-500 hover:-translate-y-2 hover:border-cyan-500/20 hover:bg-slate-900">
            <div class="mb-5 inline-flex h-12 w-12 items-center justify-center rounded-3xl bg-cyan-500/10 text-cyan-300 transition group-hover:bg-cyan-500/20">
              <app-icon name="lock" size="lg" />
            </div>
            <h3 class="text-xl font-semibold text-white">Sécurité renforcée</h3>
            <p class="mt-3 text-sm leading-7 text-slate-400">Vos données sont chiffrées et protégées, avec des accès sécurisés pour les agents et les contribuables.</p>
          </article>

          <article class="group rounded-[28px] border border-white/10 bg-slate-900/80 p-6 transition duration-500 hover:-translate-y-2 hover:border-emerald-500/20 hover:bg-slate-900">
            <div class="mb-5 inline-flex h-12 w-12 items-center justify-center rounded-3xl bg-emerald-500/10 text-emerald-300 transition group-hover:bg-emerald-500/20">
              <app-icon name="chart" size="lg" />
            </div>
            <h3 class="text-xl font-semibold text-white">Vue en temps réel</h3>
            <p class="mt-3 text-sm leading-7 text-slate-400">Analysez l’avancement des dossiers et les flux d’activité directement depuis votre tableau de bord.</p>
          </article>
        </section>
    </section>
  </main>
  `,
})
export class HomeComponent { }
