import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  BackendErrorResponse,
  ForgotPasswordRequest,
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  ResetPasswordRequest,
  RoleUtilisateur,
  Utilisateur,
} from '../models/utilisateur.model';

const TOKEN_KEY = 'token';
const ADMIN_TOKEN_KEY = 'adminToken';
const AGENT_TOKEN_KEY = 'agentToken';
const REFRESH_TOKEN_KEY = 'dgi_refresh_token';
const USER_KEY = 'dgi_utilisateur';

/** Providers supported by the OAuth gateway exposed by the DGI API. */
export type SocialAuthProvider = 'google' | 'facebook' | 'twitter';

@Injectable({ providedIn: 'root' })
export class AuthService {
  // Signal contenant l'utilisateur courant, lu depuis le localStorage au démarrage
  private readonly _utilisateurCourant = signal<Utilisateur | null>(
    this.lireUtilisateurStocke()
  );

  /** Exposé en lecture seule aux composants (ex: affichage du rôle, du nom) */
  readonly utilisateurCourant = this._utilisateurCourant.asReadonly();

  /** Signal dérivé pratique pour les guards / templates */
  readonly estConnecte = computed(() => this._utilisateurCourant() !== null);

  constructor(private http: HttpClient) { }

  login(payload: LoginRequest): Observable<LoginResponse> {
    const backendPayload = {
      email: payload.email,
      motDePasse: payload.motDePasse,
    };

    return this.http
      .post<LoginResponse>(`${environment.apiBaseUrl}/auth/login`, backendPayload)
      .pipe(tap((reponse) => this.enregistrerSession(reponse)));
  }

  register(payload: RegisterRequest): Observable<LoginResponse> {
    const backendPayload = {
      email: payload.email,
      motDePasse: payload.motDePasse,
      ...(payload.contribuableId ? { contribuableId: payload.contribuableId } : {}),
    };

    return this.http
      .post<LoginResponse>(`${environment.apiBaseUrl}/auth/register`, backendPayload)
      .pipe(tap((reponse) => this.enregistrerSession(reponse)));
  }

  /**
   * Starts the server-managed OAuth flow. Provider secrets stay on the API,
   * never in the browser application.
   */
  demarrerConnexionSociale(provider: SocialAuthProvider): void {
    if (typeof window === 'undefined') return;
    window.location.assign(`${environment.apiBaseUrl}/auth/oauth2/authorization/${provider}`);
  }

  /** Demande l'envoi d'un lien de réinitialisation à l'adresse indiquée. */
  demanderReinitialisationMotDePasse(payload: ForgotPasswordRequest): Observable<void> {
    return this.http.post<void>(`${environment.apiBaseUrl}/auth/forgot-password`, payload);
  }

  /** Réinitialise le mot de passe à partir du jeton reçu par e-mail. */
  reinitialiserMotDePasse(payload: ResetPasswordRequest): Observable<void> {
    return this.http.post<void>(`${environment.apiBaseUrl}/auth/reset-password`, payload);
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(ADMIN_TOKEN_KEY);
    localStorage.removeItem(AGENT_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    this._utilisateurCourant.set(null);
    window.location.href = '/auth/login';
  }

  getToken(): string | null {
    const role = this._utilisateurCourant()?.role;
    if (role === 'ADMIN') {
      return localStorage.getItem(ADMIN_TOKEN_KEY) ?? localStorage.getItem(TOKEN_KEY);
    }
    if (role === 'AGENT_DGI') {
      return localStorage.getItem(AGENT_TOKEN_KEY) ?? localStorage.getItem(TOKEN_KEY);
    }
    return localStorage.getItem(TOKEN_KEY);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(REFRESH_TOKEN_KEY);
  }

  /**
   * Met à jour le token après un refresh réussi (appelé par l'interceptor).
   * Ne touche pas aux infos utilisateur déjà stockées.
   */
  mettreAJourToken(nouveauToken: string): void {
    const role = this._utilisateurCourant()?.role;
    if (role === 'ADMIN') {
      localStorage.setItem(ADMIN_TOKEN_KEY, nouveauToken);
      localStorage.setItem(TOKEN_KEY, nouveauToken);
      return;
    }
    if (role === 'AGENT_DGI') {
      localStorage.setItem(AGENT_TOKEN_KEY, nouveauToken);
      localStorage.setItem(TOKEN_KEY, nouveauToken);
      return;
    }
    localStorage.setItem(TOKEN_KEY, nouveauToken);
  }

  /** Enregistre la session (token + utilisateur) après login/register réussi */
  private enregistrerSession(reponse: LoginResponse): void {
    const utilisateur = this.mapUtilisateur(reponse);
    const role = utilisateur.role;

    localStorage.setItem(TOKEN_KEY, reponse.token);
    if (role === 'ADMIN') {
      localStorage.setItem(ADMIN_TOKEN_KEY, reponse.token);
    } else {
      localStorage.removeItem(ADMIN_TOKEN_KEY);
    }

    if (role === 'AGENT_DGI') {
      localStorage.setItem(AGENT_TOKEN_KEY, reponse.token);
    } else {
      localStorage.removeItem(AGENT_TOKEN_KEY);
    }

    if (reponse.refreshToken) {
      localStorage.setItem(REFRESH_TOKEN_KEY, reponse.refreshToken);
    }
    localStorage.setItem(USER_KEY, JSON.stringify(utilisateur));
    this._utilisateurCourant.set(utilisateur);
  }

  mettreAJourPhotoProfil(photoUrl: string): void {
    const utilisateur = this._utilisateurCourant();
    if (!utilisateur) {
      return;
    }

    const utilisateurMisAJour = {
      ...utilisateur,
      photoUrl,
    };

    localStorage.setItem(USER_KEY, JSON.stringify(utilisateurMisAJour));
    this._utilisateurCourant.set(utilisateurMisAJour);
  }

  /** Met à jour les informations de profil disponibles dans la session locale. */
  mettreAJourProfilLocal(changements: Partial<Pick<Utilisateur, 'nom' | 'prenom' | 'email'>>): void {
    const utilisateur = this._utilisateurCourant();
    if (!utilisateur) return;
    const utilisateurMisAJour = { ...utilisateur, ...changements };
    localStorage.setItem(USER_KEY, JSON.stringify(utilisateurMisAJour));
    this._utilisateurCourant.set(utilisateurMisAJour);
  }

  private mapUtilisateur(reponse: LoginResponse): Utilisateur {
    const role = this.normaliserRole(reponse.role ?? 'CONTRIBUABLE');

    return {
      id: reponse.utilisateur?.id ?? reponse.contribuableId ?? '',
      email: reponse.utilisateur?.email ?? reponse.email ?? '',
      nom: reponse.utilisateur?.nom ?? '',
      prenom: reponse.utilisateur?.prenom ?? '',
      role,
      photoUrl: reponse.utilisateur?.photoUrl ?? null,
    };
  }

  parseRoleFromJwt(token: string | null): RoleUtilisateur | null {
    if (!token) {
      return null;
    }

    try {
      const payload = token.split('.')[1];
      const decoded = JSON.parse(atob(payload.replace(/-/g, '+').replace(/_/g, '/')));
      const role = decoded.role ?? decoded.roles ?? decoded.authorities ?? null;
      return this.normaliserRole(role);
    } catch {
      return null;
    }
  }

  private normaliserRole(role?: string): RoleUtilisateur {
    const valeur = (role ?? 'CONTRIBUABLE').toString().trim().toUpperCase();

    if (valeur === 'ADMIN' || valeur === 'ADMINISTRATEUR') {
      return 'ADMIN';
    }
    if (valeur === 'AGENT_DGI' || valeur === 'AGENT' || valeur === 'DGI') {
      return 'AGENT_DGI';
    }
    return 'CONTRIBUABLE';
  }

  private lireUtilisateurStocke(): Utilisateur | null {
    const brut = localStorage.getItem(USER_KEY);
    if (!brut) return null;
    try {
      return JSON.parse(brut) as Utilisateur;
    } catch {
      return null;
    }
  }
}
