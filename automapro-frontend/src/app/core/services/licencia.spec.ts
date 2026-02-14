import { TestBed } from '@angular/core/testing';

import { Licencia } from './licencia';

describe('Licencia', () => {
  let service: Licencia;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Licencia);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
