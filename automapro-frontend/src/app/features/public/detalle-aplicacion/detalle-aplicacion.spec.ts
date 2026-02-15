import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DetalleAplicacion } from './detalle-aplicacion';

describe('DetalleAplicacion', () => {
  let component: DetalleAplicacion;
  let fixture: ComponentFixture<DetalleAplicacion>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DetalleAplicacion]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DetalleAplicacion);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
