import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StepActiviteComponent } from './step-activite.component';

describe('StepActiviteComponent', () => {
  let component: StepActiviteComponent;
  let fixture: ComponentFixture<StepActiviteComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StepActiviteComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(StepActiviteComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
