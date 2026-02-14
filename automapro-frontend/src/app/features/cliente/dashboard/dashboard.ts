import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Auth } from '../../../core/services/auth';
import { LicenciaService } from '../../../core/services/licencia';
import { Licencia } from '../../../core/models/licencia.model';

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss'
})
export class Dashboard implements OnInit {
  nombreUsuario = '';
  emailUsuario = '';
  
  // Estadísticas del cliente
  totalLicencias = 0;
  licenciasActivas = 0;
  
  cargando = true;

  constructor(
    private authService: Auth,
    private licenciaService: LicenciaService
  ) {}

  ngOnInit(): void {
    // Obtener datos del usuario actual
    const usuario = this.authService.getUsuarioActual();
    this.nombreUsuario = usuario?.nombre || 'Usuario';
    this.emailUsuario = usuario?.email || '';
    
    // Cargar estadísticas del cliente
    this.cargarEstadisticas();
  }

  /**
   * Cargar estadísticas del cliente
   */
  cargarEstadisticas(): void {
    this.cargando = true;

    // Aquí necesitaríamos el ID del usuario
    // Por ahora mostramos un placeholder
    // TODO: Implementar endpoint para obtener licencias del usuario autenticado
    
    this.cargando = false;
    this.totalLicencias = 0;
    this.licenciasActivas = 0;
  }
}