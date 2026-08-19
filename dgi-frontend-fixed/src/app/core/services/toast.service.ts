import { Injectable, signal } from '@angular/core';

export interface Toast {
  id: number;
  message: string;
  type: 'success' | 'error' | 'info';
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  private readonly _toasts = signal<Toast[]>([]);
  readonly toasts = this._toasts.asReadonly();
  private compteur = 0;

  afficherSucces(message: string): void { this.ajouter(message, 'success'); }
  afficherErreur(message: string): void { this.ajouter(message, 'error'); }
  afficherInfo(message: string): void { this.ajouter(message, 'info'); }

  fermer(id: number): void {
    this._toasts.update(liste => liste.filter(t => t.id !== id));
  }

  private ajouter(message: string, type: Toast['type']): void {
    const id = ++this.compteur;
    this._toasts.update(liste => [...liste, { id, message, type }]);
    setTimeout(() => this.fermer(id), 5000);
  }
}
