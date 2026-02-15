import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { Auth } from '../../../core/services/auth';
import { RespuestaLogin } from '../../../core/models/respuesta-login.model';
import { CONSTANTES } from '../../../core/config/constantes';

@Component({
  selector: 'app-navbar',
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './navbar.html',
  styleUrl: './navbar.scss'
})
export class Navbar implements OnInit {
  usuarioActual: RespuestaLogin | null = null;
  esAdmin = false;

  constructor(
    private authService: Auth,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Suscribirse a los cambios del usuario actual
    this.authService.usuarioActual$.subscribe(usuario => {
      this.usuarioActual = usuario;
      this.esAdmin = usuario?.rol === CONSTANTES.ROLES.ADMIN;
    });
  }

  /**
   * Cerrar sesión
   */
  cerrarSesion(): void {
    this.authService.logout();
    this.router.navigate(['/catalogo']);
  }

  /**
   * Verificar si el usuario está autenticado
   */
  estaAutenticado(): boolean {
    return this.authService.estaAutenticado();
  }
}