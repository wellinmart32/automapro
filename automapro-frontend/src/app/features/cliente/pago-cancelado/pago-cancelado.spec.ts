import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PagoCancelado } from './pago-cancelado';

describe('PagoCancelado', () => {
  let component: PagoCancelado;
  let fixture: ComponentFixture<PagoCancelado>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PagoCancelado]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PagoCancelado);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
