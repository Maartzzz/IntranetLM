import { ComponentFixture, TestBed } from '@angular/core/testing';

import { QuestionImportDialogComponent } from './question-import-dialog.component';

describe('QuestionImportDialogComponent', () => {
  let component: QuestionImportDialogComponent;
  let fixture: ComponentFixture<QuestionImportDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [QuestionImportDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(QuestionImportDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
