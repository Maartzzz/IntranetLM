import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RequirementCatalogComponent } from './requirement-catalog.component';

describe('RequirementCatalogComponent', () => {
  let component: RequirementCatalogComponent;
  let fixture: ComponentFixture<RequirementCatalogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RequirementCatalogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RequirementCatalogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
