import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Auth } from '../../../core/services/auth';
import { UsuarioService } from '../../../core/services/usuario';
import { AplicacionService } from '../../../core/services/aplicacion';
import { LicenciaService } from '../../../core/services/licencia';

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss'
})
export class Dashboard implements OnInit {
  nombreUsuario = '';
  
  // Estadísticas
  totalUsuarios = 0;
  totalAplicaciones = 0;
  totalLicencias = 0;
  
  cargando = true;

  constructor(
    private authService: Auth,
    private usuarioService: UsuarioService,
    private aplicacionService: AplicacionService,
    private licenciaService: LicenciaService
  ) {}

  ngOnInit(): void {
    // Obtener nombre del usuario actual
    const usuario = this.authService.getUsuarioActual();
    this.nombreUsuario = usuario?.nombre || 'Administrador';
    
    // Cargar estadísticas
    this.cargarEstadisticas();
  }

  /**
   * Cargar estadísticas del dashboard
   */
  cargarEstadisticas(): void {
    this.cargando = true;

    // Cargar total de usuarios
    this.usuarioService.listarTodos().subscribe({
      next: (usuarios) => {
        this.totalUsuarios = usuarios.length;
      },
      error: (error) => {
        console.error('Error al cargar usuarios:', error);
      }
    });

    // Cargar total de aplicaciones
    this.aplicacionService.listarTodas().subscribe({
      next: (aplicaciones) => {
        this.totalAplicaciones = aplicaciones.length;
      },
      error: (error) => {
        console.error('Error al cargar aplicaciones:', error);
      }
    });

    // Cargar total de licencias
    this.licenciaService.listarTodas().subscribe({
      next: (licencias) => {
        this.totalLicencias = licencias.length;
        this.cargando = false;
      },
      error: (error) => {
        console.error('Error al cargar licencias:', error);
        this.cargando = false;
      }
    });
  }
}