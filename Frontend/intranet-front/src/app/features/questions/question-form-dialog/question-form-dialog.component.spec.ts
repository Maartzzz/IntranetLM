import { ComponentFixture, TestBed } from '@angular/core/testing';

import { QuestionFormDialogComponent } from './question-form-dialog.component';

describe('QuestionFormDialogComponent', () => {
  let component: QuestionFormDialogComponent;
  let fixture: ComponentFixture<QuestionFormDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [QuestionFormDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(QuestionFormDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
