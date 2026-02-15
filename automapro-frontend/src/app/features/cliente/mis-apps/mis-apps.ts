import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { API_CONFIG } from '../../../core/config/api.config';
import { Licencia } from '../../../core/models/licencia.model';

@Component({
  selector: 'app-mis-apps',
  imports: [CommonModule, RouterLink],
  templateUrl: './mis-apps.html',
  styleUrl: './mis-apps.scss'
})
export class MisApps implements OnInit {
  licencias: Licencia[] = [];
  cargando = false;
  mensajeError = '';

  constructor(
    private http: HttpClient
  ) { }

  ngOnInit(): void {
    this.cargarMisAplicaciones();
  }

  /**
   * Cargar aplicaciones del cliente actual
   */
  cargarMisAplicaciones(): void {
    this.cargando = true;

    const url = `${API_CONFIG.baseUrl}/api/cliente/mis-apps`;

    this.http.get<Licencia[]>(url).subscribe({
      next: (licencias) => {
        this.licencias = licencias;
        this.cargando = false;
      },
      error: (error) => {
        console.error('Error al cargar aplicaciones:', error);
        this.mensajeError = 'Error al cargar tus aplicaciones';
        this.cargando = false;
      }
    });
  }

  /**
   * Descargar instalador de una aplicación
   */
  descargarAplicacion(licencia: Licencia): void {
    if (!licencia.aplicacionNombre) {
      alert('No se puede descargar esta aplicación');
      return;
    }

    // Verificar si hay archivo disponible
    if (!licencia.aplicacionRutaArchivo) {
      alert(`El instalador de ${licencia.aplicacionNombre} no está disponible aún.\n\nContacta al administrador para que suba el archivo.`);
      return;
    }

    // Mostrar mensaje con código de licencia
    const mensaje = `Descargando: ${licencia.aplicacionNombre}\n\n` +
      `Tu código de licencia:\n${licencia.codigo}\n\n` +
      `IMPORTANTE: Guarda este código, lo necesitarás para activar la aplicación.\n\n` +
      `La descarga comenzará en un momento...`;

    alert(mensaje);

    // Extraer nombre del archivo de la ruta
    const nombreArchivo = this.extraerNombreArchivo(licencia.aplicacionRutaArchivo);

    if (nombreArchivo) {
      // Construir URL de descarga
      const urlDescarga = `${API_CONFIG.baseUrl}/api/archivos/descargar/${nombreArchivo}`;

      // Descargar mediante HttpClient (incluye token JWT automáticamente)
      this.http.get(urlDescarga, { responseType: 'blob', observe: 'response' }).subscribe({
        next: (response) => {
          // Crear blob y disparar descarga
          const blob = response.body;
          if (blob) {
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = nombreArchivo;
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
            window.URL.revokeObjectURL(url);
          }
        },
        error: (error) => {
          console.error('Error al descargar:', error);
          alert('Error al descargar el archivo. Intenta nuevamente.');
        }
      });
    } else {
      alert('Error al procesar la ruta del archivo. Contacta al administrador.');
    }
  }

  /**
   * Extraer nombre de archivo de la ruta almacenada
   * Ejemplo: "instaladores/app_1_123456.zip" -> "app_1_123456.zip"
   */
  private extraerNombreArchivo(ruta: string): string | null {
    if (!ruta) return null;

    // Extraer solo el nombre del archivo (última parte después de /)
    const partes = ruta.split('/');
    return partes[partes.length - 1];
  }

  /**
   * Verificar si una licencia está vigente
   */
  estaVigente(licencia: Licencia): boolean {
    if (!licencia.activo) {
      return false;
    }

    if (!licencia.fechaExpiracion) {
      return true; // Licencia permanente
    }

    const hoy = new Date();
    const expiracion = new Date(licencia.fechaExpiracion);
    return expiracion >= hoy;
  }

  /**
   * Obtener estado de la licencia en texto
   */
  getEstadoLicencia(licencia: Licencia): string {
    if (!licencia.activo) {
      return 'Inactiva';
    }

    if (licencia.tipoLicencia === 'FULL') {
      return 'Completa';
    }

    if (!licencia.fechaExpiracion) {
      return 'Trial Permanente';
    }

    const hoy = new Date();
    const expiracion = new Date(licencia.fechaExpiracion);

    if (expiracion < hoy) {
      return 'Expirada';
    }

    const diasRestantes = Math.ceil((expiracion.getTime() - hoy.getTime()) / (1000 * 60 * 60 * 24));

    if (diasRestantes <= 7) {
      return `Expira en ${diasRestantes} días`;
    }

    return `Trial - ${diasRestantes} días restantes`;
  }

  /**
   * Obtener clase CSS según el estado
   */
  getClaseEstado(licencia: Licencia): string {
    if (!licencia.activo) {
      return 'bg-secondary';
    }

    if (licencia.tipoLicencia === 'FULL') {
      return 'bg-primary';
    }

    if (!licencia.fechaExpiracion) {
      return 'bg-success';
    }

    const hoy = new Date();
    const expiracion = new Date(licencia.fechaExpiracion);

    if (expiracion < hoy) {
      return 'bg-danger';
    }

    const diasRestantes = Math.ceil((expiracion.getTime() - hoy.getTime()) / (1000 * 60 * 60 * 24));

    if (diasRestantes <= 7) {
      return 'bg-warning';
    }

    return 'bg-success';
  }

  /**
   * Copiar código al portapapeles
   */
  copiarCodigo(codigo: string): void {
    navigator.clipboard.writeText(codigo).then(() => {
      alert('Código copiado al portapapeles');
    });
  }
}