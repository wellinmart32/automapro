import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { API_CONFIG } from '../../../core/config/api.config';

@Component({
  selector: 'app-perfil',
  imports: [CommonModule, FormsModule],
  templateUrl: './perfil.html',
  styleUrl: './perfil.scss'
})
export class Perfil implements OnInit {

  perfil: any = null;
  cargando = false;

  // Editar nombre
  editandoNombre = false;
  nuevoNombre = '';
  guardandoNombre = false;
  mensajeNombre = '';
  errorNombre = '';

  // Cambiar contraseña
  passwordActual = '';
  passwordNueva = '';
  passwordConfirmar = '';
  guardandoPassword = false;
  mensajePassword = '';
  errorPassword = '';

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.cargarPerfil();
  }

  cargarPerfil(): void {
    this.cargando = true;
    this.http.get(`${API_CONFIG.baseUrl}/api/cliente/perfil`).subscribe({
      next: (perfil) => {
        this.perfil = perfil;
        this.nuevoNombre = (perfil as any).nombre;
        this.cargando = false;
      },
      error: () => {
        this.cargando = false;
      }
    });
  }

  editarNombre(): void {
    this.editandoNombre = true;
    this.mensajeNombre = '';
    this.errorNombre = '';
  }

  cancelarEditarNombre(): void {
    this.editandoNombre = false;
    this.nuevoNombre = this.perfil.nombre;
  }

  guardarNombre(): void {
    if (!this.nuevoNombre.trim()) {
      this.errorNombre = 'El nombre no puede estar vacío';
      return;
    }
    this.guardandoNombre = true;
    this.errorNombre = '';
    this.http.put(`${API_CONFIG.baseUrl}/api/cliente/perfil`, { nombre: this.nuevoNombre }).subscribe({
      next: (respuesta: any) => {
        this.perfil.nombre = respuesta.nombre;
        this.editandoNombre = false;
        this.guardandoNombre = false;
        this.mensajeNombre = 'Nombre actualizado correctamente';
        setTimeout(() => this.mensajeNombre = '', 3000);
      },
      error: (error) => {
        this.errorNombre = error.error || 'Error al actualizar nombre';
        this.guardandoNombre = false;
      }
    });
  }

  cambiarPassword(): void {
    this.errorPassword = '';
    this.mensajePassword = '';

    if (!this.passwordActual || !this.passwordNueva || !this.passwordConfirmar) {
      this.errorPassword = 'Completa todos los campos';
      return;
    }

    if (this.passwordNueva !== this.passwordConfirmar) {
      this.errorPassword = 'Las contraseñas nuevas no coinciden';
      return;
    }

    if (this.passwordNueva.length < 6) {
      this.errorPassword = 'La nueva contraseña debe tener al menos 6 caracteres';
      return;
    }

    this.guardandoPassword = true;
    this.http.put(`${API_CONFIG.baseUrl}/api/cliente/cambiar-password`, {
      passwordActual: this.passwordActual,
      passwordNueva: this.passwordNueva
    }, { responseType: 'text' }).subscribe({
      next: () => {
        this.mensajePassword = 'Contraseña actualizada correctamente';
        this.passwordActual = '';
        this.passwordNueva = '';
        this.passwordConfirmar = '';
        this.guardandoPassword = false;
        setTimeout(() => this.mensajePassword = '', 4000);
      },
      error: (error) => {
        this.errorPassword = error.error || 'Error al cambiar contraseña';
        this.guardandoPassword = false;
      }
    });
  }
}