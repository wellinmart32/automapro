import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-pago-cancelado',
  imports: [CommonModule],
  templateUrl: './pago-cancelado.html',
  styleUrl: './pago-cancelado.scss'
})
export class PagoCancelado {

  constructor(private router: Router) {}

  /**
   * Volver a intentar el pago
   */
  volverAIntentar(): void {
    this.router.navigate(['/cliente/mis-apps']);
  }

  /**
   * Ir al catálogo
   */
  irAlCatalogo(): void {
    this.router.navigate(['/catalogo']);
  }
}