import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { API_CONFIG } from '../../../core/config/api.config';

@Component({
  selector: 'app-recuperar-password',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './recuperar-password.html',
  styleUrl: './recuperar-password.scss'
})
export class RecuperarPassword {
  email = '';
  cargando = false;
  mensajeExito = '';
  mensajeError = '';

  constructor(private http: HttpClient, private router: Router) {}

  enviarSolicitud(): void {
    if (!this.email.trim()) {
      this.mensajeError = 'Ingresa tu email';
      return;
    }

    this.cargando = true;
    this.mensajeError = '';
    this.mensajeExito = '';

    this.http.post(`${API_CONFIG.baseUrl}/api/auth/recuperar-password`,
      { email: this.email },
      { responseType: 'text' }
    ).subscribe({
      next: () => {
        this.cargando = false;
        this.mensajeExito = 'Si el email existe, recibirás un enlace de recuperación en tu bandeja de entrada.';
        this.email = '';
      },
      error: () => {
        this.cargando = false;
        this.mensajeError = 'Error al procesar la solicitud. Intenta nuevamente.';
      }
    });
  }
}