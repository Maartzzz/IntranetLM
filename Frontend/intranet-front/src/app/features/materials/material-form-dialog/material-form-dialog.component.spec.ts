import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MaterialFormDialogComponent } from './material-form-dialog.component';

describe('MaterialFormDialogComponent', () => {
  let component: MaterialFormDialogComponent;
  let fixture: ComponentFixture<MaterialFormDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MaterialFormDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MaterialFormDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
