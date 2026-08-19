import { Injectable, signal } from '@angular/core';

export type LangueInterface = 'fr' | 'ar';

@Injectable({ providedIn: 'root' })
export class UiPreferencesService {
  readonly langue = signal<LangueInterface>('fr');
  readonly contrasteEleve = signal(false);

  constructor() {
    if (typeof localStorage === 'undefined') return;
    const langue = localStorage.getItem('dgi-langue');
    this.langue.set(langue === 'ar' ? 'ar' : 'fr');
    this.contrasteEleve.set(localStorage.getItem('dgi-contraste-eleve') === 'true');
    this.appliquer();
  }

  basculerLangue(): void {
    this.langue.update((langue) => langue === 'fr' ? 'ar' : 'fr');
    this.appliquer();
  }

  basculerContraste(): void {
    this.contrasteEleve.update((actif) => !actif);
    this.appliquer();
  }

  private appliquer(): void {
    if (typeof document === 'undefined' || typeof localStorage === 'undefined') return;
    const langue = this.langue();
    document.documentElement.lang = langue;
    document.documentElement.dir = langue === 'ar' ? 'rtl' : 'ltr';
    document.documentElement.classList.toggle('high-contrast', this.contrasteEleve());
    localStorage.setItem('dgi-langue', langue);
    localStorage.setItem('dgi-contraste-eleve', String(this.contrasteEleve()));
  }
}
