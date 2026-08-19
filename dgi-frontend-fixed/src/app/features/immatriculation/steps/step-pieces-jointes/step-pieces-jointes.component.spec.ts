import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StepPiecesJointesComponent } from './step-pieces-jointes.component';

describe('StepPiecesJointesComponent', () => {
  let component: StepPiecesJointesComponent;
  let fixture: ComponentFixture<StepPiecesJointesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StepPiecesJointesComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(StepPiecesJointesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
