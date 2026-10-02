import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MaterialViewerDialogComponent } from './material-viewer-dialog.component';

describe('MaterialViewerDialogComponent', () => {
  let component: MaterialViewerDialogComponent;
  let fixture: ComponentFixture<MaterialViewerDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MaterialViewerDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MaterialViewerDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
