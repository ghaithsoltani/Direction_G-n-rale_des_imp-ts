import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { IconComponent } from '../../shared/components/icon/icon.component';

@Component({
  selector: 'app-help',
  standalone: true,
  imports: [CommonModule, IconComponent],
  template: `
    <section class="mx-auto max-w-5xl space-y-6">
      <header class="rounded-[28px] bg-gradient-to-r from-slate-950 to-blue-800 p-6 text-white"><p class="text-xs font-bold uppercase tracking-[0.2em] text-blue-200">Assistance DGI</p><h1 class="mt-2 text-3xl font-semibold">Comment pouvons-nous vous aider ?</h1><p class="mt-2 text-sm text-blue-100">Consultez les réponses rapides ou contactez l’assistance.</p></header>
      <div class="grid gap-5 md:grid-cols-2">
        <article class="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm"><div class="text-blue-600"><app-icon name="robot" size="lg" /></div><h2 class="mt-3 text-xl font-semibold text-slate-900">Assistant virtuel</h2><p class="mt-2 text-sm leading-6 text-slate-600">Utilisez le bouton « Assistant DGI » en bas à droite pour une aide immédiate sur votre démarche.</p></article>
        <article class="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm"><div class="text-blue-600"><app-icon name="mail" size="lg" /></div><h2 class="mt-3 text-xl font-semibold text-slate-900">Contacter l’assistance</h2><p class="mt-2 text-sm leading-6 text-slate-600">Pour une situation personnelle ou technique, contactez notre équipe de support.</p><a href="mailto:contact@dgi.tn?subject=Assistance%20Portail%20DGI" class="mt-4 inline-flex rounded-xl bg-blue-600 px-4 py-2.5 text-sm font-semibold text-white">Écrire à l’assistance</a></article>
      </div>
      <article class="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm"><h2 class="text-xl font-semibold text-slate-900">Questions fréquentes</h2><div class="mt-4 divide-y divide-slate-100">@for (faq of faqs; track faq.question) { <details class="py-4"><summary class="cursor-pointer text-sm font-semibold text-slate-800">{{ faq.question }}</summary><p class="mt-3 max-w-3xl text-sm leading-6 text-slate-600">{{ faq.reponse }}</p></details> }</div></article>
    </section>
  `,
})
export class HelpComponent {
  readonly faqs = [
    { question: 'Comment suivre ma demande ?', reponse: 'Ouvrez le tableau de bord pour consulter le statut, la prochaine action et l’historique de votre dossier.' },
    { question: 'Quels documents dois-je fournir ?', reponse: 'La liste s’adapte à votre profil dans l’étape « Pièces jointes ». Ajoutez tous les documents marqués comme requis.' },
    { question: 'Comment corriger un dossier rejeté ?', reponse: 'Lisez le message de la DGI dans votre tableau de bord, puis sélectionnez « Corriger ma demande ».' },
    { question: 'Mon brouillon est-il conservé ?', reponse: 'Oui. Les informations du formulaire sont enregistrées automatiquement sur cet appareil jusqu’à la soumission réussie.' },
  ];
}
