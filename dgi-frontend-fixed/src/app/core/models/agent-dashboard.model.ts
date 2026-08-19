import { StatutDossier } from './statut-dossier.enum';

export interface DashboardStats {
    totalDossiers: number;
    brouillons: number;
    soumis: number;
    enTraitement: number;
    valides: number;
    rejetes: number;
}

export interface DashboardNotification {
    id: number;
    type: 'nouveau' | 'attente' | 'ocr' | 'verification' | 'valide' | 'rejete';
    titre: string;
    message: string;
    detail: string;
    lu: boolean;
    time: string;
}

export interface DashboardActivite {
    id: number;
    texte: string;
    heure: string;
    statut?: StatutDossier;
}
