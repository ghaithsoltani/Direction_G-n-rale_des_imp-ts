import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface ChatbotReponse {
  conversationId?: string | null;
  reponse: string;
  source?: 'FAQ' | 'LLM' | 'DOSSIER_STATUS' | 'ERREUR';
}

export interface ChatbotRequest {
  message: string;
  conversationId?: string | null;
}

interface AdminAiChatResponse {
  reponse?: string;
  response?: string;
  message?: string;
  conversationId?: string | null;
}

const ADMIN_DOCUMENTATION_INSTRUCTIONS = `Agis comme un rédacteur technique senior et un designer de documentation UI/UX.
Ne réponds jamais en texte brut. Produis une fiche technique Markdown professionnelle, avec cette structure lorsque pertinente :
# 📌 Titre
## 📖 Executive Summary, suivi d'un tableau Item | Description
## 📊 Quick Facts, suivi d'un tableau Feature | Value
## 🧠 Key Concepts, suivi d'un tableau Concept | Definition | Why Important | Example
## ⚙️ Architecture, avec un diagramme Mermaid dans un bloc \`\`\`mermaid.
Utilise des tableaux Markdown pour toute information structurée, reste précis et adapte les sections au besoin administratif.`;

export interface FaqEntry {
  id?: string;
  question: string;
  reponse: string;
  categorie?: string;
  motsCles?: string[];
}

@Injectable({ providedIn: 'root' })
export class ChatbotService {
  private readonly http = inject(HttpClient);

  demander(message: string, conversationId?: string | null): Observable<ChatbotReponse> {
    const payload: ChatbotRequest = { message, conversationId: conversationId ?? null };
    return this.http.post<ChatbotReponse>(`${environment.apiBaseUrl}/chatbot/message`, payload);
  }

  /** Assistant réservé aux administrateurs : le JWT est ajouté par l'intercepteur. */
  demanderAdministration(message: string): Observable<ChatbotReponse> {
    return this.http
      .post<AdminAiChatResponse>(`${environment.apiBaseUrl}/admin/ai/chat`, {
        message: `${ADMIN_DOCUMENTATION_INSTRUCTIONS}\n\nDemande de l'administrateur : ${message}`,
      })
      .pipe(map((reponse) => ({
        conversationId: reponse.conversationId ?? null,
        reponse: reponse.reponse ?? reponse.response ?? reponse.message ?? '',
      })));
  }

  listerFaq(): Observable<FaqEntry[]> {
    return this.http.get<FaqEntry[]>(`${environment.apiBaseUrl}/chatbot/faq`);
  }

  creerFaq(payload: Omit<FaqEntry, 'id'>): Observable<FaqEntry> {
    return this.http.post<FaqEntry>(`${environment.apiBaseUrl}/chatbot/faq`, payload);
  }
}
