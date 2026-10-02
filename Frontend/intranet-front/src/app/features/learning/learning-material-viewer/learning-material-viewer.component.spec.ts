import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LearningMaterialViewerComponent } from './learning-material-viewer.component';

describe('LearningMaterialViewerComponent', () => {
  let component: LearningMaterialViewerComponent;
  let fixture: ComponentFixture<LearningMaterialViewerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LearningMaterialViewerComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LearningMaterialViewerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
