import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CourseAudienceComponent } from './course-audience.component';

describe('CourseAudienceComponent', () => {
  let component: CourseAudienceComponent;
  let fixture: ComponentFixture<CourseAudienceComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CourseAudienceComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CourseAudienceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
