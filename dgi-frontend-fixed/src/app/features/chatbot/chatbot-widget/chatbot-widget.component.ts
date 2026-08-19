import { Component, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatbotService } from '../../../core/services/chatbot.service';
import { ToastService } from '../../../core/services/toast.service';
import { AuthService } from '../../../core/services/auth.service';
import { IconComponent } from '../../../shared/components/icon/icon.component';

interface MessageChat {
  id: number;
  role: 'user' | 'assistant';
  texte: string;
  document?: DocumentBlock[];
}

interface DocumentBlock {
  type: 'heading' | 'paragraph' | 'table' | 'code';
  level?: number;
  content?: string;
  headers?: string[];
  rows?: string[][];
  language?: string;
}

@Component({
  selector: 'app-chatbot-widget',
  standalone: true,
  imports: [CommonModule, FormsModule, IconComponent],
  template: `
    <div class="fixed bottom-4 right-4 z-[60]">
      <button (click)="basculer()" class="flex items-center gap-3 rounded-full bg-slate-950 px-4 py-3 text-sm font-semibold text-white shadow-[0_20px_50px_rgba(15,23,42,0.28)] transition hover:scale-105">
        <app-icon name="robot" size="sm" />
        {{ estAdmin() ? 'Assistant Admin' : 'Assistant DGI' }}
      </button>

      @if (ouvert()) {
        <div class="mt-3 w-[92vw] max-w-[430px] overflow-hidden rounded-[28px] border border-slate-200 bg-white shadow-[0_20px_60px_rgba(15,23,42,0.16)]">
          <div class="bg-gradient-to-r from-blue-600 to-blue-500 px-4 py-4 text-white">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-sm font-semibold">{{ estAdmin() ? 'Assistant administration' : 'Assistant virtuel' }}</p>
                <p class="text-xs text-blue-100">{{ estAdmin() ? 'Aide IA réservée à l’administration DGI' : 'Aide en ligne pour vos démarches fiscales' }}</p>
              </div>
              <button (click)="basculer()" class="inline-flex p-1" aria-label="Fermer l’assistant"><app-icon name="close" size="sm" /></button>
            </div>
          </div>

          <div class="flex h-[380px] flex-col bg-slate-50 p-3">
            <div class="flex-1 space-y-2 overflow-y-auto rounded-2xl bg-white p-3">
              @for (message of messages(); track message.id) {
                <div class="flex" [class.justify-end]="message.role === 'user'">
                  <div class="max-w-[92%] rounded-2xl px-3 py-2 text-sm"
                       [ngClass]="message.role === 'user' ? 'bg-blue-600 text-white' : 'border border-slate-200 bg-slate-50 text-slate-700'">
                    @if (message.document?.length) {
                      <article class="space-y-3">
                        @for (block of message.document; track $index) {
                          @switch (block.type) {
                            @case ('heading') {
                              @if (block.level === 1) { <h2 class="text-base font-bold text-slate-950">{{ block.content }}</h2> }
                              @else { <h3 class="border-b border-slate-200 pb-1 text-sm font-semibold text-blue-800">{{ block.content }}</h3> }
                            }
                            @case ('paragraph') { <p class="leading-6 text-slate-700">{{ block.content }}</p> }
                            @case ('table') {
                              <div class="overflow-x-auto rounded-xl border border-slate-200 bg-white">
                                <table class="min-w-full text-left text-xs">
                                  <thead class="bg-blue-50 text-blue-950"><tr>@for (header of block.headers ?? []; track $index) { <th class="whitespace-nowrap px-3 py-2 font-semibold">{{ header }}</th> }</tr></thead>
                                  <tbody>@for (row of block.rows ?? []; track $index) { <tr class="border-t border-slate-100">@for (cell of row; track $index) { <td class="px-3 py-2 align-top leading-5 text-slate-700">{{ cell }}</td> }</tr> }</tbody>
                                </table>
                              </div>
                            }
                            @case ('code') {
                              <div class="overflow-x-auto rounded-xl bg-slate-950 p-3">
                                @if (block.language === 'mermaid') { <p class="mb-2 text-[10px] font-bold uppercase tracking-wider text-cyan-300">Diagramme Mermaid</p> }
                                <pre class="whitespace-pre-wrap font-mono text-xs leading-5 text-slate-100">{{ block.content }}</pre>
                              </div>
                            }
                          }
                        }
                      </article>
                    } @else {
                      {{ message.texte }}
                    }
                  </div>
                </div>
              }
              @if (chargement()) {
                <div class="flex justify-start">
                  <div class="rounded-2xl bg-slate-100 px-3 py-2 text-sm text-slate-700">Écriture...</div>
                </div>
              }
            </div>

            <div class="mt-3 flex flex-wrap gap-2">
              @for (suggestion of suggestions(); track suggestion) {
                <button (click)="remplirSuggestion(suggestion)"
                  class="rounded-full border border-slate-200 bg-white px-3 py-1.5 text-xs font-medium text-slate-600 transition hover:border-blue-300 hover:text-blue-700">
                  {{ suggestion }}
                </button>
              }
            </div>

            <!-- FIX 9: messageText est maintenant une propriété string ordinaire
                 (pas un Signal), compatible avec [(ngModel)] d'Angular -->
            <div class="mt-3 flex gap-2">
              <input
                [(ngModel)]="messageText"
                (keyup.enter)="envoyerMessage()"
                class="flex-1 rounded-2xl border border-slate-200 px-3 py-2.5 text-sm outline-none focus:border-blue-500"
                [placeholder]="estAdmin() ? 'Posez une question d’administration' : 'Posez votre question'" />
              <button (click)="envoyerMessage()" class="rounded-2xl bg-blue-600 px-4 py-2.5 text-sm font-semibold text-white">Envoyer</button>
            </div>
          </div>
        </div>
      }
    </div>
  `,
})
export class ChatbotWidgetComponent {
  private readonly chatbotService = inject(ChatbotService);
  private readonly toastService = inject(ToastService);
  private readonly authService = inject(AuthService);

  readonly ouvert = signal(false);
  readonly estAdmin = computed(() => this.authService.utilisateurCourant()?.role === 'ADMIN');
  // FIX 9: was signal('') — [(ngModel)] requires a plain mutable property, not a Signal
  messageText = '';
  readonly messages = signal<MessageChat[]>([
    { id: 1, role: 'assistant', texte: 'Bonjour, je peux vous aider sur la demande de matricule fiscal et les étapes de votre dossier.' },
  ]);
  readonly chargement = signal(false);
  readonly compteurMessages = computed(() => this.messages().length);
  readonly suggestions = computed(() => this.estAdmin()
    ? [
        'Résumé des dossiers en attente',
        'Quels indicateurs dois-je surveiller ?',
        'Aide sur la gestion des utilisateurs',
        'Signaler une anomalie de traitement',
      ]
    : [
        'Comment déposer une demande ?',
        'Où suivre mon dossier ?',
        'Quelles pièces sont obligatoires ?',
        'Comment corriger un dossier rejeté ?',
      ]);

  private conversationId: string | null = null;
  private conversationAdminInitialisee: boolean | null = null;

  basculer(): void {
    this.ouvert.update((etat) => !etat);
    if (this.ouvert()) this.initialiserConversation();
  }

  remplirSuggestion(texte: string): void {
    this.messageText = texte;
  }

  envoyerMessage(): void {
    const texte = this.messageText.trim();
    if (!texte || this.chargement()) return;

    const messageUtilisateur: MessageChat = { id: Date.now(), role: 'user', texte };
    this.messages.update((liste) => [...liste, messageUtilisateur]);
    this.messageText = '';
    this.chargement.set(true);

    const demande = this.estAdmin()
      ? this.chatbotService.demanderAdministration(texte)
      : this.chatbotService.demander(texte, this.conversationId);

    demande.subscribe({
      next: (reponse) => {
        if (reponse.conversationId) this.conversationId = reponse.conversationId;
        this.messages.update((liste) => [
          ...liste,
          {
            id: Date.now() + 1,
            role: 'assistant',
            texte: reponse.reponse || "Je n'ai pas pu générer une réponse précise.",
            document: this.estAdmin() ? this.convertirDocument(reponse.reponse) : undefined,
          },
        ]);
        this.chargement.set(false);
      },
      error: () => {
        this.chargement.set(false);
        this.toastService.afficherErreur(this.estAdmin()
          ? 'L’assistant administrateur est momentanément indisponible. Réessayez plus tard.'
          : 'Le chatbot est momentanément indisponible. Réessayez plus tard.');
      },
    });
  }

  private initialiserConversation(): void {
    if (this.conversationAdminInitialisee === this.estAdmin()) return;

    this.conversationAdminInitialisee = this.estAdmin();
    this.conversationId = null;
    this.messages.set([{
      id: Date.now(),
      role: 'assistant',
      texte: this.estAdmin()
        ? 'Bonjour, je suis l’assistant IA de l’administration DGI. Je peux vous aider à analyser les opérations et à gérer le portail.'
        : 'Bonjour, je peux vous aider sur la demande de matricule fiscal et les étapes de votre dossier.',
    }]);
  }

  /** Convertit le sous-ensemble Markdown produit par l'IA en éléments Angular sûrs, sans innerHTML. */
  private convertirDocument(markdown?: string): DocumentBlock[] | undefined {
    if (!markdown?.trim()) return undefined;

    const lignes = markdown.replace(/\r/g, '').split('\n');
    const blocs: DocumentBlock[] = [];
    let index = 0;

    while (index < lignes.length) {
      const ligne = lignes[index].trim();
      if (!ligne || ligne === '---') { index++; continue; }

      const titre = ligne.match(/^(#{1,3})\s+(.+)$/);
      if (titre) {
        blocs.push({ type: 'heading', level: titre[1].length, content: titre[2] });
        index++;
        continue;
      }

      const code = ligne.match(/^```(.*)$/);
      if (code) {
        const language = code[1].trim().toLowerCase();
        const contenu: string[] = [];
        index++;
        while (index < lignes.length && !lignes[index].trim().startsWith('```')) contenu.push(lignes[index++]);
        if (index < lignes.length) index++;
        blocs.push({ type: 'code', language, content: contenu.join('\n') });
        continue;
      }

      if (ligne.startsWith('|') && index + 1 < lignes.length && /^\s*\|?\s*:?-{3,}/.test(lignes[index + 1])) {
        const headers = this.decouperLigneTableau(lignes[index]);
        const rows: string[][] = [];
        index += 2;
        while (index < lignes.length && lignes[index].trim().startsWith('|')) rows.push(this.decouperLigneTableau(lignes[index++]));
        blocs.push({ type: 'table', headers, rows });
        continue;
      }

      const paragraphe: string[] = [ligne];
      index++;
      while (index < lignes.length && lignes[index].trim() && !/^(#{1,3})\s+|^```|^\|/.test(lignes[index].trim())) paragraphe.push(lignes[index++].trim());
      blocs.push({ type: 'paragraph', content: paragraphe.join(' ') });
    }

    return blocs;
  }

  private decouperLigneTableau(ligne: string): string[] {
    return ligne.trim().replace(/^\||\|$/g, '').split('|').map((cellule) => cellule.trim());
  }
}
