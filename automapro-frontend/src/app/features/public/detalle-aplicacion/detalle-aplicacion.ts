import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AplicacionService } from '../../../core/services/aplicacion';
import { Auth } from '../../../core/services/auth';
import { Aplicacion } from '../../../core/models/aplicacion.model';

@Component({
  selector: 'app-detalle-aplicacion',
  imports: [CommonModule, RouterLink],
  templateUrl: './detalle-aplicacion.html',
  styleUrl: './detalle-aplicacion.scss'
})
export class DetalleAplicacion implements OnInit {
  aplicacion: Aplicacion | null = null;
  cargando = false;
  mensajeError = '';
  descargando = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private aplicacionService: AplicacionService,
    private authService: Auth
  ) {}

  ngOnInit(): void {
    // Obtener ID de la ruta
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.cargarDetalle(+id);
    } else {
      this.mensajeError = 'ID de aplicación no válido';
    }
  }

  /**
   * Cargar detalle de la aplicación
   */
  cargarDetalle(id: number): void {
    this.cargando = true;
    this.aplicacionService.obtenerDetallePublico(id).subscribe({
      next: (aplicacion) => {
        this.aplicacion = aplicacion;
        this.cargando = false;
      },
      error: (error) => {
        console.error('Error al cargar detalle:', error);
        this.mensajeError = 'No se pudo cargar la información de la aplicación';
        this.cargando = false;
      }
    });
  }

  /**
   * Iniciar descarga (verifica autenticación y genera licencia TRIAL)
   */
  descargarTrial(): void {
    // Verificar si está autenticado
    if (!this.authService.estaAutenticado()) {
      // Redirigir a login
      this.router.navigate(['/login']);
      return;
    }

    // TODO: Implementar generación de licencia TRIAL y descarga
    alert('Generando licencia TRIAL y preparando descarga...\n(Funcionalidad en desarrollo)');
  }

  /**
   * Comprar versión completa
   */
  comprarCompleta(): void {
    // Verificar si está autenticado
    if (!this.authService.estaAutenticado()) {
      // Redirigir a login
      this.router.navigate(['/login']);
      return;
    }

    // TODO: Implementar proceso de compra
    alert('Proceso de compra...\n(Funcionalidad en desarrollo - integración con pasarela de pagos)');
  }

  /**
   * Volver al catálogo
   */
  volverCatalogo(): void {
    this.router.navigate(['/catalogo']);
  }

  /**
   * Formato de precio
   */
  formatoPrecio(precio?: number): string {
    if (!precio || precio === 0) {
      return 'Gratis';
    }
    return `$${precio.toFixed(2)}`;
  }
}