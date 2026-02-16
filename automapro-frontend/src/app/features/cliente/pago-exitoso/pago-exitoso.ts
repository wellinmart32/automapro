import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-pago-exitoso',
  imports: [CommonModule],
  templateUrl: './pago-exitoso.html',
  styleUrl: './pago-exitoso.scss'
})
export class PagoExitoso implements OnInit {
  sessionId: string = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Leer session_id de la URL
    this.route.queryParams.subscribe(params => {
      this.sessionId = params['session_id'] || '';
    });
  }

  /**
   * Ir a Mis Aplicaciones
   */
  irAMisApps(): void {
    this.router.navigate(['/cliente/mis-apps']);
  }
}