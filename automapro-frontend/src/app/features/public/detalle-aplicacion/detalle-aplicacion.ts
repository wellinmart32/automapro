import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AplicacionService } from '../../../core/services/aplicacion';
import { LicenciaService } from '../../../core/services/licencia';
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
  
  // Modal de licencia generada
  mostrarModalLicencia = false;
  licenciaGenerada: any = null;

  // Estado de licencia existente
  licenciaExistente: any = null;
  verificandoLicencia = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private aplicacionService: AplicacionService,
    private licenciaService: LicenciaService,
    private authService: Auth
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.cargarDetalle(+id);
      if (this.authService.estaAutenticado()) {
        this.verificarLicenciaExistente(+id);
      }
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
    if (!this.authService.estaAutenticado()) {
      this.router.navigate(['/login'], {
        queryParams: { returnUrl: this.router.url }
      });
      return;
    }

    if (!this.aplicacion || !this.aplicacion.id) {
      return;
    }

    this.descargando = true;
    this.mensajeError = '';

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
        } else if (error.error === 'Ya tienes una licencia para esta aplicación') {
          // Ya tiene licencia — recargar para mostrar botón de descarga
          this.verificarLicenciaExistente(this.aplicacion!.id!);
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

    window.open(this.aplicacion.rutaArchivo, '_blank');
  }

  /**
   * Comprar versión completa
   */
  comprarCompleta(): void {
    if (!this.aplicacion || !this.aplicacion.id) {
      return;
    }

    if (!this.authService.estaAutenticado()) {
      localStorage.setItem('returnUrl', `/cliente/comprar?app=${this.aplicacion.id}`);
      this.router.navigate(['/login']);
      return;
    }

    this.router.navigate(['/cliente/comprar'], {
      queryParams: { app: this.aplicacion.id }
    });
  }

  /**
   * Verificar si el usuario ya tiene licencia para esta app
   */
  verificarLicenciaExistente(aplicacionId: number): void {
    this.verificandoLicencia = true;
    this.licenciaService.obtenerMisApps().subscribe({
      next: (licencias) => {
        this.licenciaExistente = licencias.find((l: any) => l.aplicacionId === aplicacionId) || null;
        this.verificandoLicencia = false;
      },
      error: () => {
        this.verificandoLicencia = false;
      }
    });
  }

  /**
   * Verificar si el usuario está autenticado
   */
  estaAutenticado(): boolean {
    return this.authService.estaAutenticado();
  }

  estaVigente(licencia: any): boolean {
    if (!licencia.activo) return false;
    if (!licencia.fechaExpiracion) return true;
    return new Date(licencia.fechaExpiracion) >= new Date();
  }

  getDiasRestantes(licencia: any): number {
    if (!licencia.fechaExpiracion) return 0;
    const hoy = new Date();
    hoy.setHours(0, 0, 0, 0);
    const expiracion = new Date(licencia.fechaExpiracion);
    expiracion.setHours(0, 0, 0, 0);
    const diff = Math.ceil((expiracion.getTime() - hoy.getTime()) / (1000 * 60 * 60 * 24));
    return Math.max(0, diff);
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