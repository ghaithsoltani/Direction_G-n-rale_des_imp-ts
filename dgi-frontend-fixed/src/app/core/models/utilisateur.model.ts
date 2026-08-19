export type RoleUtilisateur = 'CONTRIBUABLE' | 'AGENT_DGI' | 'ADMIN';

export interface Utilisateur {
  id: string;
  email: string;
  nom: string;
  prenom: string;
  role: RoleUtilisateur;
  photoUrl?: string | null;
}

/** Payload envoyé à POST /api/auth/login */
export interface LoginRequest {
  email: string;
  motDePasse: string;
}

/** Réponse renvoyée par POST /api/auth/login */
export interface LoginResponse {
  token: string;
  refreshToken?: string;
  utilisateur?: Utilisateur;
  email?: string;
  role?: RoleUtilisateur | string;
  contribuableId?: string | null;
}

export interface BackendErrorResponse {
  timestamp?: string;
  status?: number;
  erreur?: string;
  message?: string;
  chemin?: string;
  erreursChamps?: Array<{ champ?: string; message?: string }>;
}

/** Payload envoyé à POST /api/auth/register */
export interface RegisterRequest {
  email: string;
  motDePasse: string;
  nom?: string;
  prenom?: string;
  contribuableId?: string | null;
}

/** Payload envoyé à POST /api/auth/forgot-password */
export interface ForgotPasswordRequest {
  email: string;
}

/** Payload envoyé à POST /api/auth/reset-password */
export interface ResetPasswordRequest {
  token: string;
  motDePasse: string;
}
