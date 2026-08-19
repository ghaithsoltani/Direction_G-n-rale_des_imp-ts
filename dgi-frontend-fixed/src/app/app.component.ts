import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ToastComponent } from './shared/components/toast/toast.component';
import { ChatbotWidgetComponent } from './features/chatbot/chatbot-widget/chatbot-widget.component';
import { UiPreferencesService } from './core/services/ui-preferences.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, CommonModule, ToastComponent, ChatbotWidgetComponent],
  template: `
    <router-outlet />
    <app-toast />
    <app-chatbot-widget />
  `,
})
export class AppComponent {
  // Initialise les préférences de langue, direction et contraste avant le rendu des routes publiques.
  private readonly uiPreferences = inject(UiPreferencesService);
}
