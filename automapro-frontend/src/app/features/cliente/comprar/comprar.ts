import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { AplicacionService } from '../../../core/services/aplicacion';
import { Aplicacion } from '../../../core/models/aplicacion.model';
import { Auth } from '../../../core/services/auth';

@Component({
  selector: 'app-comprar',
  imports: [CommonModule],
  templateUrl: './comprar.html',
  styleUrl: './comprar.scss'
})
export class Comprar implements OnInit {
  aplicacion: Aplicacion | null = null;
  codigoLicencia: string = '';

  // Estados
  cargando = false;
  procesandoPago = false;
  mensajeError = '';

  // Enlaces de pago de Hotmart por aplicación (aplicacionId -> URL de checkout)
  private readonly urlsPagoHotmart: { [aplicacionId: number]: string } = {
    1: 'PENDIENTE_URL_HOTMART_MENSAJESBIBLICOS',
    2: 'PENDIENTE_URL_HOTMART_PUBLICADORREDES'
  };

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private aplicacionService: AplicacionService,
    private authService: Auth
  ) {}

  ngOnInit(): void {
    // Leer parámetros de la URL
    this.route.queryParams.subscribe(params => {
      this.codigoLicencia = params['codigo'] || '';
      const appId = params['app'];

      if (appId) {
        this.cargarAplicacion(+appId);
      } else {
        this.mensajeError = 'No se especificó la aplicación a comprar';
      }
    });
  }

  /**
   * Cargar información de la aplicación
   */
  cargarAplicacion(id: number): void {
    this.cargando = true;
    this.aplicacionService.obtenerDetallePublico(id).subscribe({
      next: (aplicacion) => {
        this.aplicacion = aplicacion;
        this.cargando = false;
      },
      error: (error) => {
        console.error('Error al cargar aplicación:', error);
        this.mensajeError = 'No se pudo cargar la información de la aplicación';
        this.cargando = false;
      }
    });
  }

  /**
   * Iniciar proceso de pago con Hotmart
   */
  iniciarPago(): void {
    if (!this.aplicacion || !this.aplicacion.id) {
      return;
    }

    const urlPago = this.urlsPagoHotmart[this.aplicacion.id];
    if (!urlPago || urlPago.startsWith('PENDIENTE_')) {
      this.mensajeError = 'El enlace de pago aún no está configurado para esta aplicación';
      return;
    }

    this.procesandoPago = true;
    this.mensajeError = '';

    // Pre-llenar el correo del usuario para que la compra se asocie a su cuenta
    const email = this.authService.getUsuarioActual()?.email;
    const urlFinal = email
      ? `${urlPago}?email=${encodeURIComponent(email)}`
      : urlPago;

    // Redirigir al checkout de Hotmart
    window.location.href = urlFinal;
  }

  /**
   * Formatear precio en USD
   */
  formatoPrecio(precio?: number): string {
    if (!precio || precio === 0) {
      return 'Gratis';
    }
    return `$${precio.toFixed(2)}`;
  }

  /**
   * Volver a Mis Aplicaciones
   */
  volver(): void {
    this.router.navigate(['/cliente/mis-apps']);
  }
}