import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { API_CONFIG } from '../../../core/config/api.config';

@Component({
  selector: 'app-registro',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './registro.html',
  styleUrl: './registro.scss'
})
export class Registro {
  // Modelo del formulario
  formulario = {
    nombre: '',
    email: '',
    password: '',
    confirmarPassword: ''
  };

  // Estados
  cargando = false;
  mensajeError = '';
  mensajeExito = '';

  constructor(
    private http: HttpClient,
    private router: Router
  ) {}

  /**
   * Registrar nuevo usuario
   */
  registrarse(): void {
    // Validar campos
    if (!this.formulario.nombre || !this.formulario.email || !this.formulario.password || !this.formulario.confirmarPassword) {
      this.mensajeError = 'Por favor complete todos los campos';
      return;
    }

    // Validar email
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(this.formulario.email)) {
      this.mensajeError = 'Por favor ingrese un email válido';
      return;
    }

    // Validar que las contraseñas coincidan
    if (this.formulario.password !== this.formulario.confirmarPassword) {
      this.mensajeError = 'Las contraseñas no coinciden';
      return;
    }

    // Validar longitud de contraseña
    if (this.formulario.password.length < 6) {
      this.mensajeError = 'La contraseña debe tener al menos 6 caracteres';
      return;
    }

    this.cargando = true;
    this.mensajeError = '';
    this.mensajeExito = '';

    const payload = {
      nombre: this.formulario.nombre,
      email: this.formulario.email,
      password: this.formulario.password
    };

    this.http.post(`${API_CONFIG.baseUrl}${API_CONFIG.endpoints.auth}/registro`, payload, { responseType: 'text' })
      .subscribe({
        next: (response) => {
          this.cargando = false;
          this.mensajeExito = 'Registro exitoso. Redirigiendo al login...';
          
          // Redirigir al login después de 2 segundos
          setTimeout(() => {
            this.router.navigate(['/login']);
          }, 2000);
        },
        error: (error) => {
          this.cargando = false;
          console.error('Error en registro:', error);
          
          if (error.status === 0) {
            this.mensajeError = 'No se pudo conectar con el servidor. Verifique que el backend esté ejecutándose.';
          } else if (error.error && typeof error.error === 'string') {
            this.mensajeError = error.error;
          } else {
            this.mensajeError = 'Error al registrar usuario. Intente nuevamente.';
          }
        }
      });
  }
}