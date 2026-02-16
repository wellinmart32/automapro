import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { AplicacionService } from '../../../core/services/aplicacion';
import { Aplicacion } from '../../../core/models/aplicacion.model';
import { API_CONFIG } from '../../../core/config/api.config';

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

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private http: HttpClient,
    private aplicacionService: AplicacionService
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
   * Iniciar proceso de pago con Stripe
   */
  iniciarPago(): void {
    if (!this.aplicacion || !this.aplicacion.id) {
      return;
    }

    this.procesandoPago = true;
    this.mensajeError = '';

    const url = `${API_CONFIG.baseUrl}/api/pagos/crear-checkout`;
    const body = { aplicacionId: this.aplicacion.id };

    this.http.post<any>(url, body).subscribe({
      next: (response) => {
        if (response.checkoutUrl) {
          window.location.href = response.checkoutUrl;
        } else {
          this.mensajeError = 'Error al obtener URL de pago';
          this.procesandoPago = false;
        }
      },
      error: (error) => {
        console.error('Error al crear checkout:', error);
        this.mensajeError = error.error?.error || 'Error al iniciar el proceso de pago';
        this.procesandoPago = false;
      }
    });
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