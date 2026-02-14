import { TestBed } from '@angular/core/testing';

import { Aplicacion } from './aplicacion';

describe('Aplicacion', () => {
  let service: Aplicacion;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Aplicacion);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
