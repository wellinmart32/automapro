import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UsuarioService } from '../../../core/services/usuario';
import { Usuario } from '../../../core/models/usuario.model';
import { CONSTANTES } from '../../../core/config/constantes';

@Component({
  selector: 'app-usuarios',
  imports: [CommonModule, FormsModule],
  templateUrl: './usuarios.html',
  styleUrl: './usuarios.scss'
})
export class Usuarios implements OnInit {
  usuarios: Usuario[] = [];
  usuarioSeleccionado: Usuario | null = null;
  modoEdicion = false;
  
  // Formulario
  usuarioForm: Usuario = this.nuevoUsuario();
  passwordForm = '';
  
  // Estados
  cargando = false;
  guardando = false;
  mensajeError = '';
  mensajeExito = '';
  
  // Modal
  mostrarModal = false;

  // Roles disponibles
  roles = [
    { value: CONSTANTES.ROLES.ADMIN, label: 'Administrador' },
    { value: CONSTANTES.ROLES.CLIENTE, label: 'Cliente' }
  ];

  constructor(private usuarioService: UsuarioService) {}

  ngOnInit(): void {
    this.cargarUsuarios();
  }

  /**
   * Cargar lista de usuarios
   */
  cargarUsuarios(): void {
    this.cargando = true;
    this.usuarioService.listarTodos().subscribe({
      next: (usuarios) => {
        this.usuarios = usuarios;
        this.cargando = false;
      },
      error: (error) => {
        console.error('Error al cargar usuarios:', error);
        this.mensajeError = 'Error al cargar los usuarios';
        this.cargando = false;
      }
    });
  }

  /**
   * Abrir modal para crear usuario
   */
  abrirModalCrear(): void {
    this.modoEdicion = false;
    this.usuarioForm = this.nuevoUsuario();
    this.passwordForm = '';
    this.mensajeError = '';
    this.mensajeExito = '';
    this.mostrarModal = true;
  }

  /**
   * Abrir modal para editar usuario
   */
  abrirModalEditar(usuario: Usuario): void {
    this.modoEdicion = true;
    this.usuarioForm = { ...usuario };
    this.passwordForm = '';
    this.mensajeError = '';
    this.mensajeExito = '';
    this.mostrarModal = true;
  }

  /**
   * Cerrar modal
   */
  cerrarModal(): void {
    this.mostrarModal = false;
    this.usuarioForm = this.nuevoUsuario();
    this.passwordForm = '';
    this.mensajeError = '';
    this.mensajeExito = '';
  }

  /**
   * Guardar usuario (crear o actualizar)
   */
  guardarUsuario(): void {
    // Validar campos
    if (!this.usuarioForm.nombre || !this.usuarioForm.email || !this.usuarioForm.rol) {
      this.mensajeError = 'Por favor complete todos los campos obligatorios';
      return;
    }

    if (!this.modoEdicion && !this.passwordForm) {
      this.mensajeError = 'La contraseña es obligatoria para crear un usuario';
      return;
    }

    this.guardando = true;
    this.mensajeError = '';

    if (this.modoEdicion) {
      // Actualizar usuario existente
      this.usuarioService.actualizar(this.usuarioForm.id!, this.usuarioForm).subscribe({
        next: () => {
          this.mensajeExito = 'Usuario actualizado correctamente';
          this.guardando = false;
          this.cargarUsuarios();
          setTimeout(() => this.cerrarModal(), 1500);
        },
        error: (error) => {
          console.error('Error al actualizar usuario:', error);
          this.mensajeError = error.error || 'Error al actualizar el usuario';
          this.guardando = false;
        }
      });
    } else {
      // Crear nuevo usuario
      this.usuarioService.crear(this.usuarioForm, this.passwordForm).subscribe({
        next: () => {
          this.mensajeExito = 'Usuario creado correctamente';
          this.guardando = false;
          this.cargarUsuarios();
          setTimeout(() => this.cerrarModal(), 1500);
        },
        error: (error) => {
          console.error('Error al crear usuario:', error);
          this.mensajeError = error.error || 'Error al crear el usuario';
          this.guardando = false;
        }
      });
    }
  }

  /**
   * Eliminar usuario
   */
  eliminarUsuario(usuario: Usuario): void {
    if (!confirm(`¿Está seguro de eliminar el usuario ${usuario.nombre}?`)) {
      return;
    }

    this.usuarioService.eliminar(usuario.id!).subscribe({
      next: () => {
        this.mensajeExito = 'Usuario eliminado correctamente';
        this.cargarUsuarios();
        setTimeout(() => this.mensajeExito = '', 3000);
      },
      error: (error) => {
        console.error('Error al eliminar usuario:', error);
        this.mensajeError = 'Error al eliminar el usuario';
        setTimeout(() => this.mensajeError = '', 3000);
      }
    });
  }

  /**
   * Crear objeto de usuario vacío
   */
  private nuevoUsuario(): Usuario {
    return {
      nombre: '',
      email: '',
      rol: CONSTANTES.ROLES.CLIENTE,
      activo: true
    };
  }
}