import { Component, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { TrimNamePipe } from './trim-name.pipe';
import { UiPreferencesService } from '../../../core/services/ui-preferences.service';
import { IconComponent } from '../icon/icon.component';

@Component({
    selector: 'app-shell',
    standalone: true,
    imports: [CommonModule, FormsModule, RouterOutlet, RouterLink, RouterLinkActive, TrimNamePipe, IconComponent],
    templateUrl: './app-shell.component.html',
    styleUrl: './app-shell.component.scss',
})
export class AppShellComponent {
    private readonly authService = inject(AuthService);
    private readonly router = inject(Router);
    readonly preferences = inject(UiPreferencesService);

    readonly utilisateur = this.authService.utilisateurCourant;
    readonly menuOuvert = signal(false);
    readonly theme = signal<'light' | 'dark'>('light');
    readonly rechercheGlobale = signal('');
    /** Session-only: dossier searches may contain CIN or personal data. */
    readonly historiqueRecherches = signal<string[]>([]);
    readonly historiqueOuvert = signal(false);

    readonly estAgent = computed(() => {
        const role = this.utilisateur()?.role;
        return role === 'AGENT_DGI' || role === 'ADMIN';
    });

    readonly estAdmin = computed(() => this.utilisateur()?.role === 'ADMIN');

    readonly themeLabel = computed(() => this.theme() === 'dark' ? 'Activer le mode clair' : 'Activer le mode sombre');
    readonly langueLabel = computed(() => this.preferences.langue() === 'fr' ? 'Passer à l’arabe' : 'التبديل إلى الفرنسية');
    readonly contrasteLabel = computed(() => this.preferences.contrasteEleve() ? 'Désactiver le contraste élevé' : 'Activer le contraste élevé');

    readonly initiales = computed(() => {
        const user = this.utilisateur();
        const prenom = user?.prenom?.[0] ?? '';
        const nom = user?.nom?.[0] ?? '';
        return `${prenom}${nom}`.toUpperCase() || 'U';
    });

    readonly navigation = computed(() => {
        const role = this.utilisateur()?.role;
        const estAgent = role === 'AGENT_DGI';
        const estAdmin = role === 'ADMIN';

        const agentItems = [
            { id: 'dossiers', label: this.tr('Dossiers', 'الملفات'), route: '/agent/dossiers', icon: 'dossiers' },
        ];

        const contribuableItems = [
            { id: 'immatriculation', label: this.tr('Nouvelle immatriculation', 'تسجيل جديد'), route: '/immatriculation', icon: 'immatriculation' },
            { id: 'documents', label: this.tr('Documents', 'الوثائق'), route: '/immatriculation', icon: 'documents' },
        ];

        const commonItems = [
            { id: 'notifications', label: this.tr('Notifications', 'الإشعارات'), route: '/notifications', icon: 'notifications' },
            { id: 'help', label: this.tr('Aide', 'المساعدة'), route: '/help', icon: 'help' },
            { id: 'profile', label: this.tr('Profil', 'الملف الشخصي'), route: '/profile', icon: 'profile' },
        ];

        return [
            { id: 'home', label: this.tr('Accueil', 'الرئيسية'), route: '/dashboard', icon: 'home' },
            ...(estAdmin ? [...agentItems, ...contribuableItems, ...commonItems] : estAgent ? [...agentItems, ...commonItems] : [...contribuableItems, ...commonItems]),
        ];
    });

    constructor() {
        this.setTheme(this.getInitialTheme());
    }

    basculerMenu(): void {
        this.menuOuvert.update((value) => !value);
    }

    basculerTheme(): void {
        this.setTheme(this.theme() === 'light' ? 'dark' : 'light');
    }

    basculerLangue(): void { this.preferences.basculerLangue(); }
    basculerContraste(): void { this.preferences.basculerContraste(); }

    tr(francais: string, arabe: string): string { return this.preferences.langue() === 'ar' ? arabe : francais; }

    fermerMenu(): void {
        this.menuOuvert.set(false);
    }

    deconnecter(): void {
        this.authService.logout();
        this.router.navigate(['/auth/login']);
    }

    lancerRecherche(event?: SubmitEvent): void {
        event?.preventDefault();
        const recherche = this.rechercheGlobale().trim();
        if (!recherche || !this.estAgent()) return;

        this.ajouterHistorique(recherche);
        this.historiqueOuvert.set(false);
        this.router.navigate(['/agent/dossiers'], { queryParams: { recherche } });
    }

    effacerRecherche(): void {
        this.rechercheGlobale.set('');
        if (this.estAgent()) {
            this.router.navigate(['/agent/dossiers'], { queryParams: { recherche: null }, queryParamsHandling: 'merge' });
        }
    }

    ouvrirHistorique(): void { this.historiqueOuvert.set(true); }

    fermerHistoriqueApresDelai(): void {
        // Lets a recent-search button receive its click before the menu closes.
        setTimeout(() => this.historiqueOuvert.set(false), 150);
    }

    rechercherDepuisHistorique(recherche: string): void {
        this.rechercheGlobale.set(recherche);
        this.lancerRecherche();
    }

    viderHistorique(): void { this.historiqueRecherches.set([]); }

    private ajouterHistorique(recherche: string): void {
        this.historiqueRecherches.update((historique) => [
            recherche,
            ...historique.filter((item) => item.toLocaleLowerCase() !== recherche.toLocaleLowerCase()),
        ].slice(0, 5));
    }

    onPhotoSelected(event: Event): void {
        const input = event.target as HTMLInputElement;
        const file = input.files?.[0];
        if (!file || !file.type.startsWith('image/')) {
            return;
        }

        const reader = new FileReader();
        reader.onload = () => {
            const result = reader.result;
            if (typeof result === 'string') {
                this.authService.mettreAJourPhotoProfil(result);
            }
        };
        reader.readAsDataURL(file);
        input.value = '';
    }

    private getInitialTheme(): 'light' | 'dark' {
        if (typeof window === 'undefined') {
            return 'light';
        }

        const savedTheme = localStorage.getItem('dgi-theme');
        if (savedTheme === 'light' || savedTheme === 'dark') {
            return savedTheme;
        }

        return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
    }

    private setTheme(theme: 'light' | 'dark'): void {
        this.theme.set(theme);

        if (typeof document !== 'undefined') {
            document.documentElement.classList.toggle('dark', theme === 'dark');
        }

        if (typeof localStorage !== 'undefined') {
            localStorage.setItem('dgi-theme', theme);
        }
    }
}
