import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { DossierService } from '../../core/services/dossier.service';
import { DossierImmatriculation } from '../../core/models/dossier-immatriculation.model';
import { StatutDossier } from '../../core/models/statut-dossier.enum';
import { IconComponent } from '../../shared/components/icon/icon.component';

type EtapeSuivi = { label: string; etat: 'complete' | 'current' | 'upcoming' };

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, DatePipe, IconComponent],
  template: `
    <section class="mx-auto max-w-6xl space-y-6">
      <header class="rounded-[32px] bg-gradient-to-br from-slate-950 via-blue-950 to-blue-700 p-6 text-white shadow-[0_20px_60px_rgba(15,23,42,0.18)] sm:p-8">
        <p class="text-sm font-medium uppercase tracking-[0.26em] text-blue-200">Mon espace contribuable</p>
        <div class="mt-3 flex flex-col gap-5 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <h1 class="text-3xl font-semibold sm:text-4xl">Bonjour{{ prenom() ? ', ' + prenom() : '' }}</h1>
            <p class="mt-3 max-w-2xl text-sm leading-6 text-blue-100 sm:text-base">Retrouvez l’avancement de vos demandes et les actions à effectuer pour finaliser votre immatriculation.</p>
          </div>
          <a routerLink="/immatriculation" class="inline-flex shrink-0 items-center justify-center rounded-2xl bg-white px-5 py-3 text-sm font-semibold text-blue-700 shadow-lg transition hover:bg-blue-50">+ Nouvelle demande</a>
        </div>
      </header>

      @if (chargement()) {
        <div class="rounded-[28px] border border-slate-200 bg-white p-10 text-center text-sm text-slate-500"><span class="mr-2 inline-block h-4 w-4 animate-spin rounded-full border-2 border-blue-600 border-t-transparent align-middle"></span>Chargement de vos dossiers...</div>
      } @else {
        @if (dossierActif(); as dossier) {
        <article class="overflow-hidden rounded-[28px] border border-slate-200 bg-white shadow-sm">
          <div class="flex flex-col gap-4 border-b border-slate-100 p-6 sm:flex-row sm:items-center sm:justify-between">
            <div>
              <p class="text-sm font-semibold text-slate-900">{{ dossier.numeroDossier || 'Demande d’immatriculation' }}</p>
              <p class="mt-1 text-sm text-slate-500">Dernière mise à jour : {{ (dossier.dateDerniereModification || dossier.dateSoumission || dossier.dateCreation) | date:'d MMMM y':'':'fr' }}</p>
            </div>
            <span class="w-fit rounded-full px-3 py-1.5 text-xs font-bold" [ngClass]="badgeClass(dossier.statut)">{{ statutLabel(dossier.statut) }}</span>
          </div>

          <div class="p-6">
            <div class="grid gap-3 sm:grid-cols-5">
              @for (etape of etapesSuivi(dossier.statut); track etape.label) {
                <div class="relative flex items-center gap-2 sm:block">
                  <span class="flex h-8 w-8 shrink-0 items-center justify-center rounded-full text-sm font-bold" [ngClass]="{
                    'bg-emerald-500 text-white': etape.etat === 'complete',
                    'bg-blue-600 text-white ring-4 ring-blue-100': etape.etat === 'current',
                    'bg-slate-100 text-slate-400': etape.etat === 'upcoming'
                  }">{{ etape.etat === 'complete' ? '✓' : $index + 1 }}</span>
                  <p class="text-xs font-semibold sm:mt-2" [ngClass]="etape.etat === 'upcoming' ? 'text-slate-400' : 'text-slate-700'">{{ etape.label }}</p>
                </div>
              }
            </div>

            <div class="mt-7 flex flex-col gap-4 rounded-2xl border border-blue-100 bg-blue-50 p-4 sm:flex-row sm:items-center sm:justify-between">
              <div>
                <p class="text-sm font-bold text-slate-900">{{ prochaineAction(dossier.statut).titre }}</p>
                <p class="mt-1 text-sm text-slate-600">{{ prochaineAction(dossier.statut).description }}</p>
              </div>
              @if (dossier.statut === StatutDossier.BROUILLON || dossier.statut === StatutDossier.REJETE) {
                <a routerLink="/immatriculation" class="shrink-0 rounded-xl bg-blue-600 px-4 py-2.5 text-center text-sm font-semibold text-white transition hover:bg-blue-700">{{ dossier.statut === StatutDossier.REJETE ? 'Corriger ma demande' : 'Continuer ma demande' }}</a>
              }
            </div>

            @if (dossier.statut === StatutDossier.REJETE && dossier.commentaireAgent) {
              <div class="mt-4 rounded-2xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-800"><span class="font-bold">Message de la DGI : </span>{{ dossier.commentaireAgent }}</div>
            }
          </div>
        </article>
      } @else {
        <article class="rounded-[28px] border border-dashed border-slate-300 bg-white p-8 text-center shadow-sm">
          <div class="mx-auto flex h-14 w-14 items-center justify-center rounded-2xl bg-blue-100 text-blue-700"><app-icon name="documents" size="lg" /></div>
          <h2 class="mt-4 text-xl font-semibold text-slate-900">Vous n’avez pas encore de demande</h2>
          <p class="mx-auto mt-2 max-w-md text-sm leading-6 text-slate-500">Démarrez votre immatriculation en ligne. Votre progression sera enregistrée et visible ici.</p>
          <a routerLink="/immatriculation" class="mt-5 inline-flex rounded-2xl bg-blue-600 px-5 py-3 text-sm font-semibold text-white transition hover:bg-blue-700">Démarrer mon immatriculation</a>
        </article>
        }
      }

      <div class="grid gap-5 md:grid-cols-3">
        <article class="rounded-3xl border border-slate-200 bg-white p-5 shadow-sm"><p class="text-xs font-bold uppercase tracking-[0.18em] text-slate-400">Dossiers</p><p class="mt-3 text-3xl font-semibold text-slate-900">{{ dossiers().length }}</p><p class="mt-1 text-sm text-slate-500">demande{{ dossiers().length > 1 ? 's' : '' }} enregistrée{{ dossiers().length > 1 ? 's' : '' }}</p></article>
        <article class="rounded-3xl border border-slate-200 bg-white p-5 shadow-sm"><p class="text-xs font-bold uppercase tracking-[0.18em] text-slate-400">À compléter</p><p class="mt-3 text-3xl font-semibold text-amber-600">{{ dossiersBrouillons() }}</p><p class="mt-1 text-sm text-slate-500">brouillon{{ dossiersBrouillons() > 1 ? 's' : '' }} à finaliser</p></article>
        <article class="rounded-3xl border border-slate-200 bg-white p-5 shadow-sm"><p class="text-xs font-bold uppercase tracking-[0.18em] text-slate-400">Validées</p><p class="mt-3 text-3xl font-semibold text-emerald-600">{{ dossiersValides() }}</p><p class="mt-1 text-sm text-slate-500">immatriculation{{ dossiersValides() > 1 ? 's' : '' }} finalisée{{ dossiersValides() > 1 ? 's' : '' }}</p></article>
      </div>

      @if (dossierActif(); as dossier) {
        <article class="rounded-[28px] border border-slate-200 bg-white p-6 shadow-sm">
          <div class="flex flex-wrap items-center justify-between gap-3"><div><p class="text-xs font-bold uppercase tracking-[0.18em] text-slate-400">Historique</p><h2 class="mt-1 text-xl font-semibold text-slate-900">Activité de votre demande</h2></div><span class="text-sm text-slate-500">{{ dossier.numeroDossier || 'Demande en cours' }}</span></div>
          <ol class="mt-6 space-y-5 border-l-2 border-slate-100 pl-5">
            @for (activite of activites(dossier); track activite.texte) {
              <li class="relative"><span class="absolute -left-[1.78rem] top-1 h-4 w-4 rounded-full border-4 border-white" [ngClass]="activite.couleur"></span><p class="text-sm font-semibold text-slate-800">{{ activite.texte }}</p><p class="mt-1 text-xs text-slate-500">{{ activite.date | date:'d MMMM y, HH:mm':'':'fr' }}</p></li>
            }
          </ol>
        </article>
      }
    </section>
  `,
})
export class DashboardComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly dossierService = inject(DossierService);

  readonly StatutDossier = StatutDossier;
  readonly dossiers = signal<DossierImmatriculation[]>([]);
  readonly chargement = signal(true);
  readonly prenom = computed(() => this.authService.utilisateurCourant()?.prenom ?? '');
  readonly dossiersBrouillons = computed(() => this.dossiers().filter((d) => d.statut === StatutDossier.BROUILLON).length);
  readonly dossiersValides = computed(() => this.dossiers().filter((d) => d.statut === StatutDossier.VALIDE).length);
  readonly dossierActif = computed(() => [...this.dossiers()].sort((a, b) => (b.dateDerniereModification ?? b.dateCreation ?? '').localeCompare(a.dateDerniereModification ?? a.dateCreation ?? ''))[0] ?? null);

  ngOnInit(): void {
    const contribuableId = this.authService.utilisateurCourant()?.id;
    if (!contribuableId) {
      this.chargement.set(false);
      return;
    }

    this.dossierService.obtenirParContribuable(contribuableId)
      .pipe(finalize(() => this.chargement.set(false)))
      .subscribe({ next: (dossiers) => this.dossiers.set(dossiers ?? []), error: () => this.dossiers.set([]) });
  }

  etapesSuivi(statut: StatutDossier): EtapeSuivi[] {
    const index = ({ BROUILLON: 0, SOUMIS: 1, EN_TRAITEMENT: 2, VALIDE: 4, REJETE: 2 } as Record<StatutDossier, number>)[statut];
    return ['Brouillon', 'Envoyée', 'En cours d’examen', 'Décision DGI', 'Finalisée'].map((label, position) => ({
      label,
      etat: position < index ? 'complete' : position === index ? 'current' : 'upcoming',
    }));
  }

  statutLabel(statut: StatutDossier): string {
    return ({ BROUILLON: 'Brouillon', SOUMIS: 'Envoyée', EN_TRAITEMENT: 'En traitement', VALIDE: 'Validée', REJETE: 'À corriger' } as Record<StatutDossier, string>)[statut];
  }

  badgeClass(statut: StatutDossier): string {
    return ({ BROUILLON: 'bg-slate-100 text-slate-700', SOUMIS: 'bg-blue-100 text-blue-700', EN_TRAITEMENT: 'bg-amber-100 text-amber-800', VALIDE: 'bg-emerald-100 text-emerald-800', REJETE: 'bg-rose-100 text-rose-800' } as Record<StatutDossier, string>)[statut];
  }

  prochaineAction(statut: StatutDossier): { titre: string; description: string } {
    return ({
      BROUILLON: { titre: 'Votre demande est prête à être complétée', description: 'Ajoutez les informations et documents manquants, puis envoyez votre demande.' },
      SOUMIS: { titre: 'Votre demande a été transmise', description: 'Elle attend sa prise en charge par un agent de la DGI.' },
      EN_TRAITEMENT: { titre: 'Votre dossier est en cours d’examen', description: 'La DGI analyse les informations transmises. Vous serez averti de la décision.' },
      VALIDE: { titre: 'Votre immatriculation est validée', description: 'Votre dossier a été approuvé par la DGI.' },
      REJETE: { titre: 'Une correction est nécessaire', description: 'Consultez le message de la DGI, corrigez votre demande et soumettez-la à nouveau.' },
    } as Record<StatutDossier, { titre: string; description: string }>)[statut];
  }

  activites(dossier: DossierImmatriculation): Array<{ texte: string; date?: string; couleur: string }> {
    const activites = [{ texte: 'Demande créée', date: dossier.dateCreation, couleur: 'bg-slate-400' }];
    if (dossier.dateSoumission) activites.push({ texte: 'Demande envoyée à la DGI', date: dossier.dateSoumission, couleur: 'bg-blue-500' });
    if (dossier.statut === StatutDossier.EN_TRAITEMENT) activites.push({ texte: 'Dossier pris en charge par la DGI', date: dossier.dateDerniereModification, couleur: 'bg-amber-500' });
    if (dossier.statut === StatutDossier.VALIDE) activites.push({ texte: 'Immatriculation validée', date: dossier.dateDerniereModification, couleur: 'bg-emerald-500' });
    if (dossier.statut === StatutDossier.REJETE) activites.push({ texte: 'Correction demandée par la DGI', date: dossier.dateDerniereModification, couleur: 'bg-rose-500' });
    return activites.filter((activite) => !!activite.date);
  }
}
