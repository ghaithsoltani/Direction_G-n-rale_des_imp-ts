import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StepWebcamComponent } from './step-webcam.component';

describe('StepWebcamComponent', () => {
  let component: StepWebcamComponent;
  let fixture: ComponentFixture<StepWebcamComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StepWebcamComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(StepWebcamComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
