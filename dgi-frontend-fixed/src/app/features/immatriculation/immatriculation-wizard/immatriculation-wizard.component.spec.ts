import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ImmatriculationWizardComponent } from './immatriculation-wizard.component';

describe('ImmatriculationWizardComponent', () => {
  let component: ImmatriculationWizardComponent;
  let fixture: ComponentFixture<ImmatriculationWizardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ImmatriculationWizardComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ImmatriculationWizardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
