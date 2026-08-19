import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StepRecapitulatifComponent } from './step-recapitulatif.component';

describe('StepRecapitulatifComponent', () => {
  let component: StepRecapitulatifComponent;
  let fixture: ComponentFixture<StepRecapitulatifComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StepRecapitulatifComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(StepRecapitulatifComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
