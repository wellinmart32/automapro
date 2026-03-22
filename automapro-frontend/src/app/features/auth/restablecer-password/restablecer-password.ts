import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink, ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { API_CONFIG } from '../../../core/config/api.config';

@Component({
  selector: 'app-restablecer-password',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './restablecer-password.html',
  styleUrl: './restablecer-password.scss'
})
export class RestablecerPassword implements OnInit {
  token = '';
  password = '';
  confirmarPassword = '';
  verPassword = false;
  verConfirmar = false;
  cargando = false;
  mensajeExito = '';
  mensajeError = '';
  tokenInvalido = false;

  constructor(
    private http: HttpClient,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.token = params['token'] || '';
      if (!this.token) {
        this.tokenInvalido = true;
        this.mensajeError = 'Enlace inválido. Solicita uno nuevo.';
      }
    });
  }

  restablecerPassword(): void {
    this.mensajeError = '';

    if (!this.password || !this.confirmarPassword) {
      this.mensajeError = 'Completa todos los campos';
      return;
    }

    if (this.password !== this.confirmarPassword) {
      this.mensajeError = 'Las contraseñas no coinciden';
      return;
    }

    if (this.password.length < 6) {
      this.mensajeError = 'La contraseña debe tener al menos 6 caracteres';
      return;
    }

    this.cargando = true;
    this.http.post(`${API_CONFIG.baseUrl}/api/auth/reset-password`,
      { token: this.token, password: this.password },
      { responseType: 'text' }
    ).subscribe({
      next: () => {
        this.cargando = false;
        this.mensajeExito = '¡Contraseña actualizada! Redirigiendo al login...';
        setTimeout(() => this.router.navigate(['/login']), 3000);
      },
      error: (error) => {
        this.cargando = false;
        this.mensajeError = error.error || 'Error al restablecer contraseña. El enlace puede haber expirado.';
      }
    });
  }
}