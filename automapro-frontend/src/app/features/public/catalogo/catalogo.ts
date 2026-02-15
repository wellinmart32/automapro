import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { AplicacionService } from '../../../core/services/aplicacion';
import { Aplicacion } from '../../../core/models/aplicacion.model';

@Component({
  selector: 'app-catalogo',
  imports: [CommonModule, RouterLink],
  templateUrl: './catalogo.html',
  styleUrl: './catalogo.scss'
})
export class Catalogo implements OnInit {
  aplicaciones: Aplicacion[] = [];
  cargando = false;
  mensajeError = '';

  constructor(
    private aplicacionService: AplicacionService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.cargarCatalogo();
  }

  /**
   * Cargar catálogo público de aplicaciones
   */
  cargarCatalogo(): void {
    this.cargando = true;
    this.aplicacionService.obtenerCatalogoPublico().subscribe({
      next: (aplicaciones) => {
        this.aplicaciones = aplicaciones;
        this.cargando = false;
      },
      error: (error) => {
        console.error('Error al cargar catálogo:', error);
        this.mensajeError = 'Error al cargar el catálogo de aplicaciones';
        this.cargando = false;
      }
    });
  }

  /**
   * Navegar al detalle de una aplicación
   */
  verDetalle(id: number): void {
    this.router.navigate(['/aplicacion', id]);
  }

  /**
   * Formato de precio
   */
  formatoPrecio(precio?: number): string {
    if (!precio || precio === 0) {
      return 'Gratis';
    }
    return `$${precio.toFixed(2)}`;
  }
}