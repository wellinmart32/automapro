import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AplicacionService } from '../../../core/services/aplicacion';
import { Aplicacion } from '../../../core/models/aplicacion.model';

@Component({
  selector: 'app-aplicaciones',
  imports: [CommonModule, FormsModule],
  templateUrl: './aplicaciones.html',
  styleUrl: './aplicaciones.scss'
})
export class Aplicaciones implements OnInit {
  aplicaciones: Aplicacion[] = [];
  aplicacionSeleccionada: Aplicacion | null = null;
  modoEdicion = false;

  // Formulario
  aplicacionForm: Aplicacion = this.nuevaAplicacion();
  archivoSeleccionado: File | null = null;

  // Estados
  cargando = false;
  guardando = false;
  subiendoArchivo = false;
  mensajeError = '';
  mensajeExito = '';

  // Modal
  mostrarModal = false;
  mostrarModalArchivo = false;

  constructor(private aplicacionService: AplicacionService) { }

  ngOnInit(): void {
    this.cargarAplicaciones();
  }

  /**
   * Cargar lista de aplicaciones
   */
  cargarAplicaciones(): void {
    this.cargando = true;
    this.aplicacionService.listarTodas().subscribe({
      next: (aplicaciones) => {
        this.aplicaciones = aplicaciones;
        this.cargando = false;
      },
      error: (error) => {
        console.error('Error al cargar aplicaciones:', error);
        this.mensajeError = 'Error al cargar las aplicaciones';
        this.cargando = false;
      }
    });
  }

  /**
   * Abrir modal para crear aplicación
   */
  abrirModalCrear(): void {
    this.modoEdicion = false;
    this.aplicacionForm = this.nuevaAplicacion();
    this.mensajeError = '';
    this.mensajeExito = '';
    this.mostrarModal = true;
  }

  /**
   * Abrir modal para editar aplicación
   */
  abrirModalEditar(aplicacion: Aplicacion): void {
    this.modoEdicion = true;
    this.aplicacionForm = { ...aplicacion };
    this.mensajeError = '';
    this.mensajeExito = '';
    this.mostrarModal = true;
  }

  /**
   * Cerrar modal
   */
  cerrarModal(): void {
    this.mostrarModal = false;
    this.aplicacionForm = this.nuevaAplicacion();
    this.mensajeError = '';
    this.mensajeExito = '';
  }

  /**
   * Guardar aplicación (crear o actualizar)
   */
  guardarAplicacion(): void {
    // Validar campos
    if (!this.aplicacionForm.nombre || !this.aplicacionForm.version) {
      this.mensajeError = 'Por favor complete todos los campos obligatorios';
      return;
    }

    this.guardando = true;
    this.mensajeError = '';

    if (this.modoEdicion) {
      // Actualizar aplicación existente
      this.aplicacionService.actualizar(this.aplicacionForm.id!, this.aplicacionForm).subscribe({
        next: () => {
          this.mensajeExito = 'Aplicación actualizada correctamente';
          this.guardando = false;
          this.cargarAplicaciones();
          setTimeout(() => this.cerrarModal(), 1500);
        },
        error: (error) => {
          console.error('Error al actualizar aplicación:', error);
          this.mensajeError = error.error || 'Error al actualizar la aplicación';
          this.guardando = false;
        }
      });
    } else {
      // Crear nueva aplicación
      this.aplicacionService.crear(this.aplicacionForm).subscribe({
        next: () => {
          this.mensajeExito = 'Aplicación creada correctamente';
          this.guardando = false;
          this.cargarAplicaciones();
          setTimeout(() => this.cerrarModal(), 1500);
        },
        error: (error) => {
          console.error('Error al crear aplicación:', error);
          this.mensajeError = error.error || 'Error al crear la aplicación';
          this.guardando = false;
        }
      });
    }
  }

  /**
   * Eliminar aplicación
   */
  eliminarAplicacion(aplicacion: Aplicacion): void {
    if (!confirm(`¿Está seguro de eliminar la aplicación ${aplicacion.nombre}?`)) {
      return;
    }

    this.aplicacionService.eliminar(aplicacion.id!).subscribe({
      next: () => {
        this.mensajeExito = 'Aplicación eliminada correctamente';
        this.cargarAplicaciones();
        setTimeout(() => this.mensajeExito = '', 3000);
      },
      error: (error) => {
        console.error('Error al eliminar aplicación:', error);
        this.mensajeError = 'Error al eliminar la aplicación';
        setTimeout(() => this.mensajeError = '', 3000);
      }
    });
  }

  /**
   * Abrir modal para subir archivo
   */
  abrirModalArchivo(aplicacion: Aplicacion): void {
    this.aplicacionSeleccionada = aplicacion;
    this.archivoSeleccionado = null;
    this.mostrarModalArchivo = true;
  }

  /**
   * Cerrar modal de archivo
   */
  cerrarModalArchivo(): void {
    this.mostrarModalArchivo = false;
    this.aplicacionSeleccionada = null;
    this.archivoSeleccionado = null;
  }

  /**
   * Manejar selección de archivo
   */
  onArchivoSeleccionado(event: any): void {
    if (event.target.files && event.target.files.length > 0) {
      this.archivoSeleccionado = event.target.files[0];
    }
  }

  /**
   * Subir archivo instalador
   */
  subirArchivo(): void {
    if (!this.archivoSeleccionado || !this.aplicacionSeleccionada) {
      this.mensajeError = 'Por favor seleccione un archivo';
      return;
    }

    this.subiendoArchivo = true;
    this.mensajeError = '';

    this.aplicacionService.subirArchivo(this.aplicacionSeleccionada.id!, this.archivoSeleccionado).subscribe({
      next: () => {
        this.mensajeExito = 'Archivo subido correctamente';
        this.subiendoArchivo = false;
        this.cargarAplicaciones();
        setTimeout(() => this.cerrarModalArchivo(), 1500);
      },
      error: (error) => {
        console.error('Error al subir archivo:', error);
        this.mensajeError = 'Error al subir el archivo';
        this.subiendoArchivo = false;
      }
    });
  }

  /**
   * Crear objeto de aplicación vacío
   */
  private nuevaAplicacion(): Aplicacion {
    return {
      nombre: '',
      descripcion: '',
      version: '',
      imagenUrl: '',
      precio: 0,
      diasTrial: 30,
      activo: true
    };
  }
}