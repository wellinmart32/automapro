import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Comprar } from './comprar';

describe('Comprar', () => {
  let component: Comprar;
  let fixture: ComponentFixture<Comprar>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Comprar]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Comprar);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
