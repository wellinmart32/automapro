import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Auth } from '../../../core/services/auth';
import { SolicitudLogin } from '../../../core/models/solicitud-login.model';
import { CONSTANTES } from '../../../core/config/constantes';

@Component({
  selector: 'app-login',
  imports: [CommonModule, FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.scss'
})
export class Login {
  // Modelo del formulario
  solicitud: SolicitudLogin = {
    email: '',
    password: ''
  };

  // Estados del formulario
  cargando = false;
  mensajeError = '';

  constructor(
    private authService: Auth,
    private router: Router
  ) {}

  /**
   * Iniciar sesión
   */
  iniciarSesion(): void {
    // Validar campos
    if (!this.solicitud.email || !this.solicitud.password) {
      this.mensajeError = 'Por favor complete todos los campos';
      return;
    }

    this.cargando = true;
    this.mensajeError = '';

    this.authService.login(this.solicitud).subscribe({
      next: (respuesta) => {
        this.cargando = false;
        
        // Redirigir según el rol
        if (respuesta.rol === CONSTANTES.ROLES.ADMIN) {
          this.router.navigate(['/admin/tablero']);
        } else {
          this.router.navigate(['/cliente/tablero']);
        }
      },
      error: (error) => {
        this.cargando = false;
        console.error('Error en login:', error);
        
        if (error.status === 401) {
          this.mensajeError = 'Email o contraseña incorrectos';
        } else if (error.status === 0) {
          this.mensajeError = 'No se pudo conectar con el servidor. Verifique que el backend esté ejecutándose.';
        } else {
          this.mensajeError = error.error?.message || CONSTANTES.MENSAJES.ERROR_GENERAL;
        }
      }
    });
  }
}