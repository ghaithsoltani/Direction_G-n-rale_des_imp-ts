import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StepInfosGeneralesComponent } from './step-infos-generales.component';

describe('StepInfosGeneralesComponent', () => {
  let component: StepInfosGeneralesComponent;
  let fixture: ComponentFixture<StepInfosGeneralesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StepInfosGeneralesComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(StepInfosGeneralesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
