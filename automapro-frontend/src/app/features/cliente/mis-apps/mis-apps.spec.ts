import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MisApps } from './mis-apps';

describe('MisApps', () => {
  let component: MisApps;
  let fixture: ComponentFixture<MisApps>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MisApps]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MisApps);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
