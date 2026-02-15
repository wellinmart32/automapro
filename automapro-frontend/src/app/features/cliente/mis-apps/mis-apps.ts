import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { API_CONFIG } from '../../../core/config/api.config';
import { Licencia } from '../../../core/models/licencia.model';
import { AplicacionService } from '../../../core/services/aplicacion';

@Component({
  selector: 'app-mis-apps',
  imports: [CommonModule],
  templateUrl: './mis-apps.html',
  styleUrl: './mis-apps.scss'
})
export class MisApps implements OnInit {
  licencias: Licencia[] = [];
  cargando = false;
  mensajeError = '';

  constructor(
    private http: HttpClient,
    private aplicacionService: AplicacionService
  ) {}

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

    // Construir URL de descarga (necesitamos el nombre del archivo)
    // Por ahora mostramos alerta con el código de licencia
    alert(`Descargando: ${licencia.aplicacionNombre}\n\nTu código de licencia:\n${licencia.codigo}\n\n(Guarda este código para activar la aplicación)`);
    
    // TODO: Implementar descarga real del instalador
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