import { Component, Input } from '@angular/core';

@Component({
    selector: 'app-icon',
    standalone: true,
    template: `
    <span class="inline-flex items-center justify-center text-current" [attr.aria-hidden]="true">
      @switch (name) {
        @case ('home') {
          <svg [class]="sizeClass" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M3 10.5 12 3l9 7.5"></path>
            <path d="M5.5 9.5V21h5v-5h3v5h5V9.5"></path>
          </svg>
        }
        @case ('dossiers') {
          <svg [class]="sizeClass" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M4 7.5A2.5 2.5 0 0 1 6.5 5h3.2a2.5 2.5 0 0 1 1.8.8L12 7h6.5A2.5 2.5 0 0 1 21 9.5v8A2.5 2.5 0 0 1 18.5 20h-11A2.5 2.5 0 0 1 5 17.5z"></path>
            <path d="M5 8h5"></path>
          </svg>
        }
        @case ('immatriculation') {
          <svg [class]="sizeClass" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M7 3h8a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2z"></path>
            <path d="M8 7h8"></path>
            <path d="M8 12h5"></path>
            <path d="M8 16h6"></path>
          </svg>
        }
        @case ('documents') {
          <svg [class]="sizeClass" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M8 3h7l4 4v12a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2z"></path>
            <path d="M15 3v4h4"></path>
            <path d="M9 13h6"></path>
            <path d="M9 17h4"></path>
          </svg>
        }
        @case ('notifications') {
          <svg [class]="sizeClass" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M12 4a4 4 0 0 0-4 4v2.2c0 .7-.2 1.4-.6 2L6 14h12l-1.4-1.8a3.3 3.3 0 0 1-.6-2V8a4 4 0 0 0-4-4z"></path>
            <path d="M10 18a2 2 0 0 0 4 0"></path>
          </svg>
        }
        @case ('help') {
          <svg [class]="sizeClass" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="9"></circle>
            <path d="M9.5 9a2.8 2.8 0 0 1 5.3.7c0 1.8-2.6 2.6-2.6 3.9"></path>
            <circle cx="12" cy="16.2" r=".8"></circle>
          </svg>
        }
        @case ('profile') {
          <svg [class]="sizeClass" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="8" r="4"></circle>
            <path d="M5.5 19a6.5 6.5 0 0 1 13 0"></path>
          </svg>
        }
        @case ('search') {
          <svg [class]="sizeClass" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="11" cy="11" r="6"></circle>
            <path d="m20 20-4.2-4.2"></path>
          </svg>
        }
        @case ('menu') {
          <svg [class]="sizeClass" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M4 7h16"></path>
            <path d="M4 12h16"></path>
            <path d="M4 17h16"></path>
          </svg>
        }
        @case ('close') {
          <svg [class]="sizeClass" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M6 6l12 12"></path>
            <path d="M18 6 6 18"></path>
          </svg>
        }
        @case ('sun') {
          <svg [class]="sizeClass" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="4"></circle>
            <path d="M12 2v2"></path>
            <path d="M12 20v2"></path>
            <path d="M4.9 4.9l1.4 1.4"></path>
            <path d="M17.7 17.7l1.4 1.4"></path>
            <path d="M2 12h2"></path>
            <path d="M20 12h2"></path>
            <path d="M4.9 19.1l1.4-1.4"></path>
            <path d="M17.7 6.3l1.4-1.4"></path>
          </svg>
        }
        @case ('moon') {
          <svg [class]="sizeClass" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M20 14.7A8.7 8.7 0 1 1 9.3 4a7 7 0 0 0 10.7 10.7Z"></path>
          </svg>
        }
        @case ('language') {
          <svg [class]="sizeClass" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="9"></circle>
            <path d="M3.6 9h16.8"></path>
            <path d="M3.6 15h16.8"></path>
            <path d="M12 3a14.3 14.3 0 0 1 4 8 14.3 14.3 0 0 1-4 8 14.3 14.3 0 0 1-4-8 14.3 14.3 0 0 1 4-8Z"></path>
          </svg>
        }
        @case ('contrast') {
          <svg [class]="sizeClass" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M6 4h10a4 4 0 0 1 4 4v8a4 4 0 0 1-4 4H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2Z"></path>
            <path d="M6 8h6"></path>
            <path d="M6 12h8"></path>
          </svg>
        }
        @case ('upload') {
          <svg [class]="sizeClass" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M12 3v10"></path>
            <path d="m8 7 4-4 4 4"></path>
            <path d="M5 14v3a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2v-3"></path>
          </svg>
        }
        @case ('id-card') {
          <svg [class]="sizeClass" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <rect x="4" y="5" width="16" height="14" rx="2"></rect>
            <circle cx="12" cy="10" r="3"></circle>
            <path d="M7 17a4 4 0 0 1 10 0"></path>
          </svg>
        }
        @case ('passport') {
          <svg [class]="sizeClass" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <rect x="5" y="4" width="14" height="16" rx="2"></rect>
            <path d="M8 8h8"></path>
            <path d="M8 12h8"></path>
            <path d="M8 16h4"></path>
          </svg>
        }
        @case ('building') {
          <svg [class]="sizeClass" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M4 20V8l8-4 8 4v12"></path>
            <path d="M8 20v-6h8v6"></path>
            <path d="M10 10h4"></path>
          </svg>
        }
        @case ('file') {
          <svg [class]="sizeClass" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M8 3h7l4 4v12a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2z"></path>
            <path d="M15 3v4h4"></path>
          </svg>
        }
        @case ('check') {
          <svg [class]="sizeClass" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M20 6 9 17l-5-5"></path>
          </svg>
        }
        @case ('circle') {
          <svg [class]="sizeClass" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="8"></circle>
          </svg>
        }
        @case ('info') {
          <svg [class]="sizeClass" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="9"></circle>
            <path d="M12 8h.01"></path>
            <path d="M11 11h1v5h1"></path>
          </svg>
        }
        @case ('success') {
          <svg [class]="sizeClass" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M20 6 9 17l-5-5"></path>
          </svg>
        }
        @case ('warning') {
          <svg [class]="sizeClass" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M12 3 2 19h20L12 3Z"></path>
            <path d="M12 8v5"></path>
            <path d="M12 16h.01"></path>
          </svg>
        }
        @case ('danger') {
          <svg [class]="sizeClass" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="9"></circle>
            <path d="m9 9 6 6"></path>
            <path d="m15 9-6 6"></path>
          </svg>
        }
        @case ('arrow-left') {
          <svg [class]="sizeClass" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="m15 18-6-6 6-6"></path>
          </svg>
        }
        @case ('arrow-right') {
          <svg [class]="sizeClass" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="m9 18 6-6-6-6"></path>
          </svg>
        }
        @case ('mail') {
          <svg [class]="sizeClass" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="5" width="18" height="14" rx="2"></rect><path d="m4 7 8 6 8-6"></path></svg>
        }
        @case ('lock') {
          <svg [class]="sizeClass" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><rect x="5" y="10" width="14" height="11" rx="2"></rect><path d="M8 10V7a4 4 0 0 1 8 0v3"></path></svg>
        }
        @case ('robot') {
          <svg [class]="sizeClass" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><rect x="4" y="7" width="16" height="12" rx="3"></rect><path d="M12 3v4M8 12h.01M16 12h.01M9 16h6"></path></svg>
        }
        @case ('chart') {
          <svg [class]="sizeClass" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M4 19V5M4 19h16"></path><path d="m7 15 4-4 3 2 5-6"></path></svg>
        }
        @case ('camera') {
          <svg [class]="sizeClass" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M4 8h3l1.5-2h7L17 8h3v11H4z"></path><circle cx="12" cy="13" r="3.5"></circle></svg>
        }
        @case ('image') {
          <svg [class]="sizeClass" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="16" rx="2"></rect><circle cx="8" cy="9" r="1.5"></circle><path d="m4 18 5-5 3 3 3-3 5 5"></path></svg>
        }
        @case ('briefcase') {
          <svg [class]="sizeClass" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="7" width="18" height="13" rx="2"></rect><path d="M8 7V5h8v2M3 12h18M10 12v2h4v-2"></path></svg>
        }
        @case ('user') {
          <svg [class]="sizeClass" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="8" r="3.5"></circle><path d="M5 20a7 7 0 0 1 14 0"></path></svg>
        }
        @default {
          <svg [class]="sizeClass" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="9"></circle>
          </svg>
        }
      }
    </span>
  `,
    styles: [':host { display: inline-flex; }'],
})
export class IconComponent {
    @Input() name = 'default';
    @Input() size: 'sm' | 'md' | 'lg' = 'md';

    get sizeClass(): string {
        return this.size === 'sm' ? 'h-4 w-4' : this.size === 'lg' ? 'h-6 w-6' : 'h-5 w-5';
    }
}
