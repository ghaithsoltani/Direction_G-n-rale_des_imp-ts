import { Component, Input } from '@angular/core';
import { NgClass } from '@angular/common';

export type AuthFeatureIconName = 'file-check' | 'shield-check' | 'bell' | 'user-plus';
export type AuthFeatureIconTone = 'blue' | 'cyan' | 'green' | 'purple';

/** Lucide-compatible, 1.9px line icons used exclusively by authentication feature cards. */
@Component({
  selector: 'app-auth-feature-icon',
  standalone: true,
  imports: [NgClass],
  template: `
    <span class="flex h-12 w-12 shrink-0 items-center justify-center rounded-full"
      [ngClass]="{
        'bg-blue-500/15 text-blue-300 shadow-[0_0_20px_rgba(37,99,235,0.12)]': tone === 'blue',
        'bg-cyan-400/15 text-cyan-300 shadow-[0_0_20px_rgba(34,211,238,0.10)]': tone === 'cyan',
        'bg-emerald-400/15 text-emerald-300 shadow-[0_0_20px_rgba(52,211,153,0.10)]': tone === 'green',
        'bg-violet-400/15 text-violet-300 shadow-[0_0_20px_rgba(167,139,250,0.10)]': tone === 'purple'
      }">
      <svg class="h-[22px] w-[22px]" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
        @switch (icon) {
          @case ('file-check') {
            <path d="M14.5 2.5H6.8A2.3 2.3 0 0 0 4.5 4.8v14.4a2.3 2.3 0 0 0 2.3 2.3h10.4a2.3 2.3 0 0 0 2.3-2.3V7.3z" />
            <path d="M14.5 2.5v4.8h4.8M8.5 15.5l2.2 2.2 4.8-5" />
          }
          @case ('shield-check') {
            <path d="M12 3.2 19 6v5.2c0 4.4-2.9 7.9-7 9.6-4.1-1.7-7-5.2-7-9.6V6z" />
            <path d="m8.8 12 2.1 2.1 4.4-4.5" />
          }
          @case ('bell') {
            <path d="M18 9.5a6 6 0 0 0-12 0c0 6.4-2.5 6.4-2.5 8h17c0-1.6-2.5-1.6-2.5-8Z" />
            <path d="M10 21h4" />
          }
          @case ('user-plus') {
            <circle cx="9" cy="7.5" r="3.2" />
            <path d="M3.8 20a5.3 5.3 0 0 1 10.4 0M18 8v6M15 11h6" />
          }
        }
      </svg>
    </span>
  `,
})
export class AuthFeatureIconComponent {
  @Input({ required: true }) icon!: AuthFeatureIconName;
  @Input() tone: AuthFeatureIconTone = 'blue';
}
