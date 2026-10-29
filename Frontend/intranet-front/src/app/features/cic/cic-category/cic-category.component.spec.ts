import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CicCategoryComponent } from './cic-category.component';

describe('CicCategoryComponent', () => {
  let component: CicCategoryComponent;
  let fixture: ComponentFixture<CicCategoryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CicCategoryComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CicCategoryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
