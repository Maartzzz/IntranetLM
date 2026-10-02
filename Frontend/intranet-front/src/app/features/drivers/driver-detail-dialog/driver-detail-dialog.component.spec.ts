import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DriverDetailDialogComponent } from './driver-detail-dialog.component';

describe('DriverDetailDialogComponent', () => {
  let component: DriverDetailDialogComponent;
  let fixture: ComponentFixture<DriverDetailDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DriverDetailDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DriverDetailDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
