import { Component, AfterViewInit, OnDestroy, computed, inject, signal, ElementRef, ViewChild } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Subject, debounceTime, distinctUntilChanged, finalize, forkJoin } from 'rxjs';
import { Chart, ChartConfiguration, registerables } from 'chart.js';
import { DossierService } from '../../../core/services/dossier.service';
import { DossierImmatriculation, FiltresDossier, PageResultat } from '../../../core/models/dossier-immatriculation.model';
import { DashboardActivite, DashboardNotification, DashboardStats } from '../../../core/models/agent-dashboard.model';
import { StatistiquesDashboard } from '../../../core/models/statistiques-dashboard.model';
import { StatutDossier } from '../../../core/models/statut-dossier.enum';
import { IconComponent } from '../../../shared/components/icon/icon.component';

@Component({
  selector: 'app-dossiers-liste',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, IconComponent],
  templateUrl: './dossiers-liste.component.html',
  styleUrl: './dossiers-liste.component.scss',
})
export class DossiersListeComponent implements AfterViewInit, OnDestroy {
  private readonly dossierService = inject(DossierService);
  private readonly datePipe = inject(DatePipe);
  private readonly route = inject(ActivatedRoute);

  @ViewChild('monthlyChartCanvas') monthlyChartCanvas?: ElementRef<HTMLCanvasElement>;
  @ViewChild('statusChartCanvas')  statusChartCanvas?: ElementRef<HTMLCanvasElement>;

  private monthlyChart?: Chart;
  private statusChart?: Chart;

  readonly StatutDossier = StatutDossier;

  readonly dossiers            = signal<DossierImmatriculation[]>([]);
  /**
   * FIX 6a: totalElements = nombre total de dossiers dans la PAGE courante (avec filtres).
   * Distinct de stats.totalDossiers qui représente le total absolu sans filtre.
   * L'ancienne version mixait les deux ce qui causait l'affichage "0 dossier(s)".
   */
  readonly totalElements       = signal(0);
  readonly pageActuelle        = signal(0);
  readonly totalPages          = signal(0);
  readonly chargement          = signal(false);
  readonly erreur              = signal<string | null>(null);
  readonly filtres             = signal<FiltresDossier>({ page: 0, taille: 10 });
  readonly aPlusieursPages     = computed(() => this.totalPages() > this.pageActuelle() + 1);
  readonly rechercheSignal     = signal('');
  readonly periodeSelectionnee = signal('month');
  readonly typeSelectionne = signal('');
  readonly selection = signal<Set<string>>(new Set());
  readonly dossiersAffiches = computed(() => this.dossiers().filter((dossier) => this.correspondAuxFiltresLocaux(dossier)));
  readonly dossiersSelectionnes = computed(() => this.dossiersAffiches().filter((dossier) => !!dossier.id && this.selection().has(dossier.id)));
  readonly tousSelectionnes = computed(() => this.dossiersAffiches().length > 0 && this.dossiersAffiches().every((dossier) => !!dossier.id && this.selection().has(dossier.id)));

  readonly stats               = signal<DashboardStats | null>(null);
  readonly notifications       = signal<DashboardNotification[]>([]);
  readonly activites           = signal<DashboardActivite[]>([]);
  readonly notificationsOuvertes = signal(false);
  readonly unreadCount         = computed(() => this.notifications().filter((n) => !n.lu).length);
  readonly filtreNotification  = signal<'tout' | 'nonlu' | 'nouveau' | 'attente' | 'ocr' | 'verification' | 'valide' | 'rejete'>('tout');

  private statsReelles         = signal<StatistiquesDashboard | null>(null);
  private readonly recherche$  = new Subject<string>();

  readonly statutsDisponibles = [
    { label: 'Tous',          value: '' },
    { label: 'Brouillon',     value: StatutDossier.BROUILLON },
    { label: 'Soumis',        value: StatutDossier.SOUMIS },
    { label: 'En traitement', value: StatutDossier.EN_TRAITEMENT },
    { label: 'Validé',        value: StatutDossier.VALIDE },
    { label: 'Rejeté',        value: StatutDossier.REJETE },
  ];

  readonly periodOptions = [
    { label: "Aujourd'hui", value: 'today' },
    { label: 'Cette semaine', value: 'week' },
    { label: 'Ce mois',      value: 'month' },
    { label: 'Cette année',  value: 'year' },
  ];

  readonly typeContribuableOptions = [
    { label: 'Tous',              value: '' },
    { label: 'Personne Physique', value: 'PERSONNE_PHYSIQUE' },
    { label: 'Personne Morale',   value: 'PERSONNE_MORALE' },
  ];

  private readonly chartColors = ['#64748b', '#6366f1', '#f59e0b', '#10b981', '#ef4444'];

  constructor() {
    Chart.register(...registerables);

    this.recherche$.pipe(debounceTime(300), distinctUntilChanged()).subscribe((value) => {
      this.rechercheSignal.set(value);
      this.filtres.update((f) => ({ ...f, recherche: value.trim() || undefined, page: 0 }));
      this.chargerDossiers();
    });

    this.route.queryParamMap.subscribe((params) => {
      const recherche = params.get('recherche')?.trim() ?? '';
      if (recherche === this.rechercheSignal()) return;

      this.rechercheSignal.set(recherche);
      this.filtres.update((f) => ({ ...f, recherche: recherche || undefined, page: 0 }));
      this.chargerDossiers();
    });

    this.chargerTout();
  }

  ngAfterViewInit(): void { /* charts rendered after data loads */ }

  ngOnDestroy(): void {
    this.monthlyChart?.destroy();
    this.statusChart?.destroy();
  }

  // ─── Filtres ───────────────────────────────────────────────────────────────

  onRechercheChange(valeur: string): void {
    this.rechercheSignal.set(valeur);
    this.recherche$.next(valeur);
  }

  onStatutChange(valeur: string): void {
    this.filtres.update((f) => ({ ...f, statut: valeur ? (valeur as StatutDossier) : undefined, page: 0 }));
    this.chargerDossiers();
  }

  onPeriodeChange(valeur: string): void { this.periodeSelectionnee.set(valeur); this.selection.set(new Set()); }
  onTypeContribuableChange(valeur: string): void { this.typeSelectionne.set(valeur); this.selection.set(new Set()); }

  pagePrecedente(): void {
    if (this.filtres().page <= 0) return;
    this.filtres.update((f) => ({ ...f, page: f.page - 1 }));
    this.chargerDossiers();
  }

  pageSuivante(): void {
    if (!this.aPlusieursPages()) return;
    this.filtres.update((f) => ({ ...f, page: f.page + 1 }));
    this.chargerDossiers();
  }

  // ─── Notifications ─────────────────────────────────────────────────────────

  basculerNotifications(): void { this.notificationsOuvertes.update((v) => !v); }

  marquerCommeLu(id: number): void {
    this.notifications.update((items) => items.map((n) => n.id === id ? { ...n, lu: true } : n));
  }
  marquerToutCommeLu(): void {
    this.notifications.update((items) => items.map((n) => ({ ...n, lu: true })));
  }
  supprimerNotification(id: number): void {
    this.notifications.update((items) => items.filter((n) => n.id !== id));
  }
  filtrerNotifications(type: any): void {
    this.filtreNotification.set(type);
  }
  getNotificationsAffichees(): DashboardNotification[] {
    const filtre = this.filtreNotification();
    return this.notifications().filter((n) =>
      filtre === 'tout' ? true : filtre === 'nonlu' ? !n.lu : n.type === filtre
    );
  }

  // ─── Badges ────────────────────────────────────────────────────────────────

  getBadgeClass(statut?: StatutDossier): string {
    switch (statut) {
      case StatutDossier.BROUILLON:     return 'bg-slate-100 text-slate-700';
      case StatutDossier.SOUMIS:        return 'bg-blue-50 text-blue-700';
      case StatutDossier.EN_TRAITEMENT: return 'bg-amber-50 text-amber-700';
      case StatutDossier.VALIDE:        return 'bg-emerald-50 text-emerald-700';
      case StatutDossier.REJETE:        return 'bg-rose-50 text-rose-700';
      default:                          return 'bg-slate-100 text-slate-700';
    }
  }

  getBadgeLabel(statut?: StatutDossier): string {
    switch (statut) {
      case StatutDossier.BROUILLON:     return 'BROUILLON';
      case StatutDossier.SOUMIS:        return 'SOUMIS';
      case StatutDossier.EN_TRAITEMENT: return 'EN TRAITEMENT';
      case StatutDossier.VALIDE:        return 'VALIDÉ';
      case StatutDossier.REJETE:        return 'REJETÉ';
      default:                          return 'INCONNU';
    }
  }

  getContribuableLabel(dossier: DossierImmatriculation): string {
    return dossier.contribuable?.raisonSociale
      || `${dossier.contribuable?.nom ?? ''} ${dossier.contribuable?.prenom ?? ''}`.trim()
      || 'Contribuable';
  }

  /**
   * FIX 6b: Formate une date ISO en format français lisible.
   * Ex: "2026-08-01T11:17:37" → "01 août 2026"
   * Remplace l'affichage brut du timestamp ISO dans le template HTML.
   */
  formatDate(dateIso?: string): string {
    if (!dateIso) return '—';
    return this.datePipe.transform(dateIso, 'd MMM yyyy', undefined, 'fr') ?? dateIso;
  }

  // ─── Actions dossier ───────────────────────────────────────────────────────

  peutTraiter(dossier: DossierImmatriculation): boolean {
    return dossier.statut === StatutDossier.SOUMIS;
  }

  peutValider(dossier: DossierImmatriculation): boolean {
    return dossier.statut === StatutDossier.EN_TRAITEMENT;
  }

  peutRejeter(dossier: DossierImmatriculation): boolean {
    return dossier.statut === StatutDossier.EN_TRAITEMENT;
  }

  traiterDossier(dossier: DossierImmatriculation): void {
    if (!dossier.id || !this.peutTraiter(dossier)) return;
    this.appliquerStatut(dossier.id, StatutDossier.EN_TRAITEMENT, "Dossier pris en charge par l'agent DGI");
  }
  validerDossier(dossier: DossierImmatriculation): void {
    if (!dossier.id || !this.peutValider(dossier)) return;
    this.appliquerStatut(dossier.id, StatutDossier.VALIDE, "Dossier validé par l'agent DGI");
  }
  rejeterDossier(dossier: DossierImmatriculation): void {
    if (!dossier.id || !this.peutRejeter(dossier)) return;
    this.appliquerStatut(dossier.id, StatutDossier.REJETE, "Dossier rejeté par l'agent DGI");
  }

  basculerSelection(id?: string): void {
    if (!id) return;
    this.selection.update((selection) => {
      const suivante = new Set(selection);
      suivante.has(id) ? suivante.delete(id) : suivante.add(id);
      return suivante;
    });
  }

  basculerToutesSelections(): void {
    if (this.tousSelectionnes()) { this.selection.set(new Set()); return; }
    this.selection.set(new Set(this.dossiersAffiches().map((dossier) => dossier.id).filter((id): id is string => !!id)));
  }

  appliquerActionMultiple(statut: StatutDossier): void {
    const ids = this.dossiersSelectionnes()
      .filter((dossier) => this.peutPasserAuStatut(dossier, statut))
      .map((dossier) => dossier.id)
      .filter((id): id is string => !!id);
    if (!ids.length) return;
    const commentaire = statut === StatutDossier.EN_TRAITEMENT ? 'Dossiers pris en charge par lot par un agent DGI' : 'Dossiers mis à jour par lot par un agent DGI';
    this.chargement.set(true);
    forkJoin(ids.map((id) => this.dossierService.changerStatut(id, statut, commentaire)))
      .pipe(finalize(() => this.chargement.set(false)))
      .subscribe({
        next: () => { this.selection.set(new Set()); this.chargerTout(); },
        error: () => this.erreur.set('Certaines mises à jour groupées ont échoué. Veuillez réessayer.'),
      });
  }

  private appliquerStatut(id: string, statut: StatutDossier, commentaire: string): void {
    this.dossierService.changerStatut(id, statut, commentaire).subscribe({
      next: () => this.chargerTout(),
      error: () => this.erreur.set('Impossible de mettre à jour le statut du dossier.'),
    });
  }

  peutPasserAuStatut(dossier: DossierImmatriculation, statut: StatutDossier): boolean {
    return (statut === StatutDossier.EN_TRAITEMENT && this.peutTraiter(dossier))
      || (statut === StatutDossier.VALIDE && this.peutValider(dossier))
      || (statut === StatutDossier.REJETE && this.peutRejeter(dossier));
  }

  nombreSelectionnesEligibles(statut: StatutDossier): number {
    return this.dossiersSelectionnes().filter((dossier) => this.peutPasserAuStatut(dossier, statut)).length;
  }

  // ─── Chargement ────────────────────────────────────────────────────────────

  chargerTout(): void {
    this.chargement.set(true);
    this.erreur.set(null);

    forkJoin({
      page: this.dossierService.lister(this.filtres()),
      stats: this.dossierService.obtenirStatistiques(),
    }).pipe(finalize(() => this.chargement.set(false)))
      .subscribe({
        next: ({ page, stats }) => {
          const dossiers = page.content ?? page.contenu ?? [];
          this.dossiers.set(dossiers);
          this.selection.set(new Set());

          // FIX 6a: totalElements = résultat paginé (avec filtres actifs)
          this.totalElements.set(page.totalElements ?? dossiers.length);
          this.totalPages.set(page.totalPages ?? 1);
          this.pageActuelle.set(page.pageActuelle ?? page.number ?? 0);

          this.statsReelles.set(stats);
          // Les KPI cards utilisent stats réelles (toujours le total global)
          this.stats.set({
            totalDossiers: stats.totalDossiers,
            brouillons:    stats.brouillons,
            soumis:        stats.soumis,
            enTraitement:  stats.enTraitement,
            valides:       stats.valides,
            rejetes:       stats.rejetes,
          });

          this.notifications.set(this.construireNotifications(dossiers));
          this.activites.set(this.construireActivites(dossiers));
          setTimeout(() => this.renderCharts(), 0);
        },
        error: () => {
          this.chargerDossiers();
        },
      });
  }

  chargerDossiers(): void {
    this.chargement.set(true);
    this.erreur.set(null);
    this.dossierService.lister(this.filtres())
      .pipe(finalize(() => this.chargement.set(false)))
      .subscribe({
        next: (page) => {
          const dossiers = page.content ?? page.contenu ?? [];
          this.dossiers.set(dossiers);
          this.selection.set(new Set());
          this.totalElements.set(page.totalElements ?? dossiers.length);
          this.totalPages.set(page.totalPages ?? 1);
          this.pageActuelle.set(page.pageActuelle ?? page.number ?? 0);
          this.notifications.set(this.construireNotifications(dossiers));
          this.activites.set(this.construireActivites(dossiers));
          setTimeout(() => this.renderCharts(), 0);
        },
        error: () => {
          this.dossiers.set([]);
          this.totalElements.set(0);
          this.totalPages.set(1);
          this.pageActuelle.set(0);
          this.erreur.set('Impossible de charger les dossiers pour le moment.');
        },
      });
  }

  // ─── Graphiques ────────────────────────────────────────────────────────────

  private renderCharts(): void {
    const sr = this.statsReelles();
    const monthlyData: number[] = sr?.evolutionMensuelle ?? Array(12).fill(0);
    const s = this.stats();
    const statusData = [
      s?.brouillons ?? 0,
      s?.soumis ?? 0,
      s?.enTraitement ?? 0,
      s?.valides ?? 0,
      s?.rejetes ?? 0,
    ];

    if (this.monthlyChartCanvas?.nativeElement) this.renderMonthlyChart(monthlyData);
    if (this.statusChartCanvas?.nativeElement)  this.renderStatusChart(statusData);
  }

  private renderMonthlyChart(data: number[]): void {
    const ctx = this.monthlyChartCanvas?.nativeElement;
    if (!ctx) return;
    this.monthlyChart?.destroy();
    this.monthlyChart = new Chart(ctx, {
      type: 'bar',
      data: {
        labels: ['Jan', 'Fév', 'Mar', 'Avr', 'Mai', 'Jun', 'Jul', 'Aoû', 'Sep', 'Oct', 'Nov', 'Déc'],
        datasets: [{ label: 'Dossiers créés', data, backgroundColor: '#2563eb', borderRadius: 8 }],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { display: false } },
        scales: { y: { beginAtZero: true, ticks: { precision: 0 } } },
      },
    } as ChartConfiguration);
  }

  private renderStatusChart(data: number[]): void {
    const ctx = this.statusChartCanvas?.nativeElement;
    if (!ctx) return;
    this.statusChart?.destroy();
    this.statusChart = new Chart(ctx, {
      type: 'doughnut',
      data: {
        labels: ['Brouillon', 'Soumis', 'Traitement', 'Validé', 'Rejeté'],
        datasets: [{ data, backgroundColor: this.chartColors, hoverOffset: 8 }],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { position: 'bottom' } },
      },
    } as ChartConfiguration);
  }

  // ─── Helpers privés ────────────────────────────────────────────────────────

  private construireNotifications(dossiers: DossierImmatriculation[]): DashboardNotification[] {
    return dossiers.slice(0, 6).map((dossier, index) => {
      const type: DashboardNotification['type'] =
        dossier.statut === StatutDossier.SOUMIS  ? 'nouveau' :
        dossier.statut === StatutDossier.VALIDE  ? 'valide'  :
        dossier.statut === StatutDossier.REJETE  ? 'rejete'  : 'attente';

      const titre =
        dossier.statut === StatutDossier.SOUMIS  ? 'Nouveau dossier soumis' :
        dossier.statut === StatutDossier.VALIDE  ? 'Dossier validé'         :
        dossier.statut === StatutDossier.REJETE  ? 'Dossier rejeté'         : 'Dossier à traiter';

      return {
        id: index + 1,
        type,
        titre,
        message: `${this.getContribuableLabel(dossier)} • ${this.getBadgeLabel(dossier.statut)}`,
        detail: dossier.numeroDossier ?? dossier.id ?? '—',
        lu: index > 2,
        time: this.formatDate(dossier.dateDerniereModification ?? dossier.dateSoumission ?? dossier.dateCreation),
      };
    });
  }

  private correspondAuxFiltresLocaux(dossier: DossierImmatriculation): boolean {
    if (this.typeSelectionne() && dossier.contribuable?.typeContribuable !== this.typeSelectionne()) return false;
    const date = dossier.dateDerniereModification ?? dossier.dateSoumission ?? dossier.dateCreation;
    if (!date || this.periodeSelectionnee() === 'year') return true;
    const jours = this.periodeSelectionnee() === 'today' ? 1 : this.periodeSelectionnee() === 'week' ? 7 : 31;
    return Date.parse(date) >= Date.now() - jours * 24 * 60 * 60 * 1000;
  }

  private construireActivites(dossiers: DossierImmatriculation[]): DashboardActivite[] {
    return dossiers.slice(0, 4).map((dossier, index) => ({
      id: index + 1,
      texte: `${this.getContribuableLabel(dossier)} • ${this.getBadgeLabel(dossier.statut)}`,
      heure: this.formatDate(dossier.dateDerniereModification ?? dossier.dateSoumission ?? dossier.dateCreation),
      statut: dossier.statut,
    }));
  }
}
