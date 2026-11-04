
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { EmployeeClaims } from './claims';

describe('EmployeeClaims', () => {
  let component: EmployeeClaims;
  let fixture: ComponentFixture<EmployeeClaims>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EmployeeClaims]
    }).compileComponents();

    fixture = TestBed.createComponent(EmployeeClaims);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
