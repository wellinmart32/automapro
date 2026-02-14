import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MisAppscls } from './mis-appscls';

describe('MisAppscls', () => {
  let component: MisAppscls;
  let fixture: ComponentFixture<MisAppscls>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MisAppscls]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MisAppscls);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
