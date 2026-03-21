import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink, ActivatedRoute } from '@angular/router';
import { Auth } from '../../../core/services/auth';
import { SolicitudLogin } from '../../../core/models/solicitud-login.model';
import { CONSTANTES } from '../../../core/config/constantes';

@Component({
  selector: 'app-login',
  imports: [CommonModule, FormsModule, RouterLink],
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

  returnUrl: string = '/cliente/tablero';

  constructor(
    private authService: Auth,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.route.queryParams.subscribe(params => {
      if (params['returnUrl']) {
        this.returnUrl = params['returnUrl'];
      }
    });
    // Leer returnUrl desde sessionStorage (usado cuando viene con query params complejos)
    const storedUrl = sessionStorage.getItem('returnUrl');
    if (storedUrl) {
      this.returnUrl = storedUrl;
      sessionStorage.removeItem('returnUrl');
    }
  }

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
          this.router.navigate([this.returnUrl]);
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