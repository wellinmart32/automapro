import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { AplicacionService } from '../../../core/services/aplicacion';
import { LicenciaService } from '../../../core/services/licencia';
import { Auth } from '../../../core/services/auth';
import { Aplicacion } from '../../../core/models/aplicacion.model';

@Component({
  selector: 'app-detalle-aplicacion',
  imports: [CommonModule],
  templateUrl: './detalle-aplicacion.html',
  styleUrl: './detalle-aplicacion.scss'
})
export class DetalleAplicacion implements OnInit {
  aplicacion: Aplicacion | null = null;
  cargando = false;
  mensajeError = '';
  descargando = false;
  
  // Modal de licencia generada
  mostrarModalLicencia = false;
  licenciaGenerada: any = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private aplicacionService: AplicacionService,
    private licenciaService: LicenciaService,
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

    if (!this.aplicacion || !this.aplicacion.id) {
      return;
    }

    this.descargando = true;
    this.mensajeError = '';

    // Generar licencia TRIAL
    this.licenciaService.generarLicenciaTrial(this.aplicacion.id).subscribe({
      next: (respuesta) => {
        this.descargando = false;
        this.licenciaGenerada = respuesta;
        this.mostrarModalLicencia = true;
      },
      error: (error) => {
        this.descargando = false;
        console.error('Error al generar licencia:', error);
        
        if (error.status === 401) {
          this.mensajeError = 'Debes iniciar sesión para descargar';
          this.router.navigate(['/login']);
        } else if (error.error && typeof error.error === 'string') {
          this.mensajeError = error.error;
        } else {
          this.mensajeError = 'Error al generar licencia. Intenta nuevamente.';
        }
      }
    });
  }

  /**
   * Cerrar modal de licencia
   */
  cerrarModalLicencia(): void {
    this.mostrarModalLicencia = false;
    this.licenciaGenerada = null;
  }

  /**
   * Descargar instalador
   */
  descargarInstalador(): void {
    if (!this.aplicacion?.rutaArchivo) {
      alert('El instalador no está disponible aún');
      return;
    }

    // Construir URL de descarga
    const nombreArchivo = this.aplicacion.rutaArchivo.split('/').pop() || '';
    const urlDescarga = this.aplicacionService.descargarArchivo(nombreArchivo);
    
    // Abrir en nueva pestaña para descargar
    window.open(urlDescarga, '_blank');
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
   * Copiar código de licencia al portapapeles
   */
  copiarCodigo(): void {
    if (!this.licenciaGenerada?.licencia?.codigo) {
      return;
    }

    navigator.clipboard.writeText(this.licenciaGenerada.licencia.codigo).then(() => {
      alert('Código copiado al portapapeles');
    });
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