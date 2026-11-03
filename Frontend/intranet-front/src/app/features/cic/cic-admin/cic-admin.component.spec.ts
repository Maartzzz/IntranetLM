import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CicAdminComponent } from './cic-admin.component';

describe('CicAdminComponent', () => {
  let component: CicAdminComponent;
  let fixture: ComponentFixture<CicAdminComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CicAdminComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CicAdminComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
