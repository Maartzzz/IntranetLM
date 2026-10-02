import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CourseDetailDialogComponent } from './course-detail-dialog.component';

describe('CourseDetailDialogComponent', () => {
  let component: CourseDetailDialogComponent;
  let fixture: ComponentFixture<CourseDetailDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CourseDetailDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CourseDetailDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
