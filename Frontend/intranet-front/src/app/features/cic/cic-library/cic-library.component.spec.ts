import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CicLibraryComponent } from './cic-library.component';

describe('CicLibraryComponent', () => {
  let component: CicLibraryComponent;
  let fixture: ComponentFixture<CicLibraryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CicLibraryComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CicLibraryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
