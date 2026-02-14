import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Licencias } from './licencias';

describe('Licencias', () => {
  let component: Licencias;
  let fixture: ComponentFixture<Licencias>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Licencias]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Licencias);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
