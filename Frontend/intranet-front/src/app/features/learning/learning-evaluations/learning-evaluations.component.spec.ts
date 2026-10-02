import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LearningEvaluatonsComponent } from './learning-evaluations.component';

describe('LearningEvaluatonsComponent', () => {
  let component: LearningEvaluatonsComponent;
  let fixture: ComponentFixture<LearningEvaluatonsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LearningEvaluatonsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LearningEvaluatonsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
