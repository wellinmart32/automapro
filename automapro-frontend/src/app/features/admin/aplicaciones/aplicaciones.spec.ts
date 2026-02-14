import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Aplicaciones } from './aplicaciones';

describe('Aplicaciones', () => {
  let component: Aplicaciones;
  let fixture: ComponentFixture<Aplicaciones>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Aplicaciones]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Aplicaciones);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
