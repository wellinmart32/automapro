import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LicenciaService } from '../../../core/services/licencia';
import { UsuarioService } from '../../../core/services/usuario';
import { AplicacionService } from '../../../core/services/aplicacion';
import { Licencia } from '../../../core/models/licencia.model';
import { Usuario } from '../../../core/models/usuario.model';
import { Aplicacion } from '../../../core/models/aplicacion.model';

@Component({
  selector: 'app-licencias',
  imports: [CommonModule, FormsModule],
  templateUrl: './licencias.html',
  styleUrl: './licencias.scss'
})
export class Licencias implements OnInit {
  licencias: Licencia[] = [];
  usuarios: Usuario[] = [];
  aplicaciones: Aplicacion[] = [];

  licenciaSeleccionada: Licencia | null = null;
  modoEdicion = false;

  // Formulario
  licenciaForm: Licencia = this.nuevaLicencia();

  // Estados
  cargando = false;
  guardando = false;
  mensajeError = '';
  mensajeExito = '';

  // Modal
  mostrarModal = false;

  constructor(
    private licenciaService: LicenciaService,
    private usuarioService: UsuarioService,
    private aplicacionService: AplicacionService
  ) { }

  ngOnInit(): void {
    this.cargarDatos();
  }

  /**
   * Cargar todos los datos necesarios
   */
  cargarDatos(): void {
    this.cargando = true;

    // Cargar licencias
    this.licenciaService.listarTodas().subscribe({
      next: (licencias) => {
        this.licencias = licencias;
      },
      error: (error) => {
        console.error('Error al cargar licencias:', error);
        this.mensajeError = 'Error al cargar las licencias';
      }
    });

    // Cargar usuarios
    this.usuarioService.listarTodos().subscribe({
      next: (usuarios) => {
        this.usuarios = usuarios;
      },
      error: (error) => {
        console.error('Error al cargar usuarios:', error);
      }
    });

    // Cargar aplicaciones
    this.aplicacionService.listarTodas().subscribe({
      next: (aplicaciones) => {
        this.aplicaciones = aplicaciones;
        this.cargando = false;
      },
      error: (error) => {
        console.error('Error al cargar aplicaciones:', error);
        this.cargando = false;
      }
    });
  }

  /**
   * Abrir modal para crear licencia
   */
  abrirModalCrear(): void {
    this.modoEdicion = false;
    this.licenciaForm = this.nuevaLicencia();
    this.mensajeError = '';
    this.mensajeExito = '';
    this.mostrarModal = true;
  }

  /**
   * Abrir modal para editar licencia
   */
  abrirModalEditar(licencia: Licencia): void {
    this.modoEdicion = true;
    this.licenciaForm = { ...licencia };

    // Convertir fecha de string a Date si es necesario
    if (licencia.fechaExpiracion && typeof licencia.fechaExpiracion === 'string') {
      this.licenciaForm.fechaExpiracion = new Date(licencia.fechaExpiracion);
    }

    this.mensajeError = '';
    this.mensajeExito = '';
    this.mostrarModal = true;
  }

  /**
   * Cerrar modal
   */
  cerrarModal(): void {
    this.mostrarModal = false;
    this.licenciaForm = this.nuevaLicencia();
    this.mensajeError = '';
    this.mensajeExito = '';
  }

  /**
   * Guardar licencia (crear o actualizar)
   */
  guardarLicencia(): void {
    // Validar campos
    if (!this.licenciaForm.usuarioId || !this.licenciaForm.aplicacionId || !this.licenciaForm.codigo) {
      this.mensajeError = 'Por favor complete todos los campos obligatorios';
      return;
    }

    this.guardando = true;
    this.mensajeError = '';

    if (this.modoEdicion) {
      // Actualizar licencia existente
      this.licenciaService.actualizar(this.licenciaForm.id!, this.licenciaForm).subscribe({
        next: () => {
          this.mensajeExito = 'Licencia actualizada correctamente';
          this.guardando = false;
          this.cargarDatos();
          setTimeout(() => this.cerrarModal(), 1500);
        },
        error: (error) => {
          console.error('Error al actualizar licencia:', error);
          this.mensajeError = error.error || 'Error al actualizar la licencia';
          this.guardando = false;
        }
      });
    } else {
      // Crear nueva licencia
      this.licenciaService.crear(this.licenciaForm).subscribe({
        next: () => {
          this.mensajeExito = 'Licencia creada correctamente';
          this.guardando = false;
          this.cargarDatos();
          setTimeout(() => this.cerrarModal(), 1500);
        },
        error: (error) => {
          console.error('Error al crear licencia:', error);
          this.mensajeError = error.error || 'Error al crear la licencia';
          this.guardando = false;
        }
      });
    }
  }

  /**
   * Eliminar licencia
   */
  eliminarLicencia(licencia: Licencia): void {
    if (!confirm(`¿Está seguro de eliminar la licencia ${licencia.codigo}?`)) {
      return;
    }

    this.licenciaService.eliminar(licencia.id!).subscribe({
      next: () => {
        this.mensajeExito = 'Licencia eliminada correctamente';
        this.cargarDatos();
        setTimeout(() => this.mensajeExito = '', 3000);
      },
      error: (error) => {
        console.error('Error al eliminar licencia:', error);
        this.mensajeError = 'Error al eliminar la licencia';
        setTimeout(() => this.mensajeError = '', 3000);
      }
    });
  }

  /**
 * Manejar cambio de fecha de expiración
 */
  onFechaExpiracionChange(value: any): void {
    this.licenciaForm.fechaExpiracion = value ? new Date(value) : undefined;
  }

  /**
   * Generar código de licencia aleatorio
   */
  generarCodigo(): void {
    const caracteres = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
    let codigo = 'LIC-';
    for (let i = 0; i < 8; i++) {
      codigo += caracteres.charAt(Math.floor(Math.random() * caracteres.length));
    }
    this.licenciaForm.codigo = codigo;
  }

  /**
   * Crear objeto de licencia vacío
   */
  private nuevaLicencia(): Licencia {
    return {
      usuarioId: 0,
      aplicacionId: 0,
      codigo: '',
      activo: true
    };
  }
}