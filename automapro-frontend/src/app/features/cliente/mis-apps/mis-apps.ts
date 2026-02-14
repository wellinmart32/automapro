import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Auth } from '../../../core/services/auth';
import { LicenciaService } from '../../../core/services/licencia';
import { AplicacionService } from '../../../core/services/aplicacion';
import { Licencia } from '../../../core/models/licencia.model';

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
    private authService: Auth,
    private licenciaService: LicenciaService,
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
    
    // Por ahora mostramos mensaje placeholder
    // TODO: Implementar endpoint en backend para obtener licencias por usuario autenticado
    this.cargando = false;
    this.licencias = [];
    this.mensajeError = 'Funcionalidad en desarrollo. Próximamente podrá ver y descargar sus aplicaciones.';
  }

  /**
   * Descargar instalador de una aplicación
   */
  descargarAplicacion(licencia: Licencia): void {
    if (!licencia.aplicacionId) {
      alert('No se puede descargar esta aplicación');
      return;
    }

    // TODO: Implementar descarga real
    alert(`Descargando: ${licencia.aplicacionNombre}`);
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

    if (!licencia.fechaExpiracion) {
      return 'Permanente';
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

    return 'Vigente';
  }

  /**
   * Obtener clase CSS según el estado
   */
  getClaseEstado(licencia: Licencia): string {
    if (!licencia.activo) {
      return 'bg-secondary';
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
}