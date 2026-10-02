import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CourseQuestionDialogComponent } from './course-question-dialog.component';

describe('CourseQuestionDialogComponent', () => {
  let component: CourseQuestionDialogComponent;
  let fixture: ComponentFixture<CourseQuestionDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CourseQuestionDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CourseQuestionDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
