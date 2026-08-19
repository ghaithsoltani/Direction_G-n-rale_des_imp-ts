import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { DossiersListeComponent } from './dossiers-liste.component';
import { StatutDossier } from '../../../core/models/statut-dossier.enum';

describe('DossiersListeComponent', () => {
  let component: DossiersListeComponent;
  let fixture: ComponentFixture<DossiersListeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DossiersListeComponent, HttpClientTestingModule],
    }).compileComponents();

    fixture = TestBed.createComponent(DossiersListeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should use the backend-compatible processing status', () => {
    expect(component.getBadgeLabel(StatutDossier.EN_TRAITEMENT)).toBe('EN TRAITEMENT');
    expect(component.getBadgeClass(StatutDossier.EN_TRAITEMENT)).toContain('amber');
  });
});
