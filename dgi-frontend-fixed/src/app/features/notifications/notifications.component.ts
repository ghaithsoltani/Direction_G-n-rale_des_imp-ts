import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { DossierService } from '../../core/services/dossier.service';
import { DossierImmatriculation } from '../../core/models/dossier-immatriculation.model';
import { StatutDossier } from '../../core/models/statut-dossier.enum';
import { IconComponent } from '../../shared/components/icon/icon.component';

interface Notification {
  id: string;
  titre: string;
  message: string;
  date?: string;
  lu: boolean;
  niveau: 'info' | 'success' | 'warning' | 'danger';
}

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [CommonModule, DatePipe, RouterLink, IconComponent],
  template: `
    <section class="mx-auto max-w-4xl space-y-6">
      <header class="rounded-[28px] bg-gradient-to-r from-slate-950 to-blue-800 p-6 text-white shadow-lg sm:flex sm:items-end sm:justify-between">
        <div><p class="text-xs font-bold uppercase tracking-[0.2em] text-blue-200">Centre de notifications</p><h1 class="mt-2 text-3xl font-semibold">Restez informé</h1><p class="mt-2 text-sm text-blue-100">Les événements importants de vos demandes apparaissent ici.</p></div>
        @if (nonLues()) { <button (click)="toutMarquerLu()" class="mt-4 rounded-xl bg-white/15 px-4 py-2 text-sm font-semibold transition hover:bg-white/25 sm:mt-0">Tout marquer comme lu</button> }
      </header>

      @if (chargement()) { <div class="rounded-3xl border border-slate-200 bg-white p-8 text-center text-sm text-slate-500">Chargement des notifications...</div> }
      @else if (!notifications().length) { <div class="rounded-3xl border border-dashed border-slate-300 bg-white p-10 text-center"><div class="flex justify-center"><app-icon name="notifications" size="lg"></app-icon></div><h2 class="mt-3 text-lg font-semibold text-slate-900">Aucune notification pour le moment</h2><p class="mt-2 text-sm text-slate-500">Les mises à jour de votre dossier apparaîtront ici.</p><a routerLink="/immatriculation" class="mt-5 inline-flex rounded-xl bg-blue-600 px-4 py-2.5 text-sm font-semibold text-white">Créer une demande</a></div> }
      @else {
        <div class="space-y-3">
          @for (notification of notifications(); track notification.id) {
            <article (click)="marquerLu(notification.id)" class="cursor-pointer rounded-2xl border p-4 transition hover:border-blue-200 hover:shadow-sm" [ngClass]="notification.lu ? 'border-slate-200 bg-white' : 'border-blue-200 bg-blue-50/70'">
              <div class="flex gap-3"><span class="mt-0.5 text-lg"><app-icon [name]="icone(notification.niveau)" size="md"></app-icon></span><div class="min-w-0 flex-1"><div class="flex flex-wrap items-center gap-2"><h2 class="text-sm font-bold text-slate-900">{{ notification.titre }}</h2>@if (!notification.lu) { <span class="h-2 w-2 rounded-full bg-blue-600" aria-label="Non lu"></span> }</div><p class="mt-1 text-sm leading-6 text-slate-600">{{ notification.message }}</p>@if (notification.date) { <p class="mt-2 text-xs text-slate-400">{{ notification.date | date:'d MMMM y, HH:mm':'':'fr' }}</p> }</div></div>
            </article>
          }
        </div>
      }
    </section>
  `,
})
export class NotificationsComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly dossierService = inject(DossierService);
  private readonly stockageKey = 'dgi-notifications-lues';
  readonly chargement = signal(true);
  readonly notifications = signal<Notification[]>([]);
  readonly nonLues = computed(() => this.notifications().filter((item) => !item.lu).length);

  ngOnInit(): void {
    const id = this.authService.utilisateurCourant()?.id;
    if (!id) { this.chargement.set(false); return; }
    this.dossierService.obtenirParContribuable(id).pipe(finalize(() => this.chargement.set(false))).subscribe({
      next: (dossiers) => this.notifications.set(this.creerNotifications(dossiers ?? [])),
      error: () => this.notifications.set([]),
    });
  }

  marquerLu(id: string): void {
    this.notifications.update((items) => items.map((item) => item.id === id ? { ...item, lu: true } : item));
    this.enregistrerLus();
  }

  toutMarquerLu(): void { this.notifications.update((items) => items.map((item) => ({ ...item, lu: true }))); this.enregistrerLus(); }
  icone(niveau: Notification['niveau']): string { return ({ info: 'info', success: 'success', warning: 'warning', danger: 'danger' })[niveau]; }

  private creerNotifications(dossiers: DossierImmatriculation[]): Notification[] {
    const lus = this.lireLus();
    return dossiers.map((dossier) => {
      const config = this.configStatut(dossier.statut);
      const id = `${dossier.id ?? dossier.numeroDossier ?? 'dossier'}-${dossier.statut}`;
      return { id, ...config, date: dossier.dateDerniereModification ?? dossier.dateSoumission ?? dossier.dateCreation, lu: lus.has(id) };
    }).sort((a, b) => (b.date ?? '').localeCompare(a.date ?? ''));
  }

  private configStatut(statut: StatutDossier): Omit<Notification, 'id' | 'date' | 'lu'> {
    return ({
      BROUILLON: { titre: 'Demande à compléter', message: 'Votre brouillon est enregistré. Vous pouvez reprendre votre demande à tout moment.', niveau: 'info' },
      SOUMIS: { titre: 'Demande envoyée', message: 'Votre dossier a bien été transmis à la DGI.', niveau: 'success' },
      EN_TRAITEMENT: { titre: 'Dossier en cours d’examen', message: 'Un agent de la DGI analyse les informations transmises.', niveau: 'warning' },
      VALIDE: { titre: 'Immatriculation validée', message: 'Votre demande a été approuvée par la DGI.', niveau: 'success' },
      REJETE: { titre: 'Correction demandée', message: 'Consultez votre tableau de bord pour corriger et soumettre à nouveau votre dossier.', niveau: 'danger' },
    } as Record<StatutDossier, Omit<Notification, 'id' | 'date' | 'lu'>>)[statut];
  }

  private lireLus(): Set<string> { try { return new Set(JSON.parse(localStorage.getItem(this.stockageKey) ?? '[]') as string[]); } catch { return new Set(); } }
  private enregistrerLus(): void { localStorage.setItem(this.stockageKey, JSON.stringify(this.notifications().filter((item) => item.lu).map((item) => item.id))); }
}
