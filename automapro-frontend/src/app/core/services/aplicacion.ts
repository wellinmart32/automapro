import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Aplicacion } from '../models/aplicacion.model';
import { API_CONFIG } from '../config/api.config';

/**
 * Servicio para gestionar operaciones CRUD de aplicaciones
 */
@Injectable({
  providedIn: 'root',
})
export class AplicacionService {
  private apiUrl = `${API_CONFIG.baseUrl}${API_CONFIG.endpoints.admin.aplicaciones}`;
  private apiArchivos = `${API_CONFIG.baseUrl}${API_CONFIG.endpoints.admin.archivos}`;

  constructor(private http: HttpClient) {}

  /**
   * Listar todas las aplicaciones
   */
  listarTodas(): Observable<Aplicacion[]> {
    return this.http.get<Aplicacion[]>(this.apiUrl);
  }

  /**
   * Listar solo aplicaciones activas
   */
  listarActivas(): Observable<Aplicacion[]> {
    return this.http.get<Aplicacion[]>(`${this.apiUrl}/activas`);
  }

  /**
   * Obtener una aplicación por ID
   */
  obtenerPorId(id: number): Observable<Aplicacion> {
    return this.http.get<Aplicacion>(`${this.apiUrl}/${id}`);
  }

  /**
   * Buscar aplicaciones por nombre
   */
  buscarPorNombre(nombre: string): Observable<Aplicacion[]> {
    return this.http.get<Aplicacion[]>(`${this.apiUrl}/buscar?nombre=${nombre}`);
  }

  /**
   * Crear una nueva aplicación
   */
  crear(aplicacion: Aplicacion): Observable<Aplicacion> {
    return this.http.post<Aplicacion>(this.apiUrl, aplicacion);
  }

  /**
   * Actualizar una aplicación existente
   */
  actualizar(id: number, aplicacion: Aplicacion): Observable<Aplicacion> {
    return this.http.put<Aplicacion>(`${this.apiUrl}/${id}`, aplicacion);
  }

  /**
   * Eliminar una aplicación (borrado lógico)
   */
  eliminar(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }

  /**
   * Subir archivo instalador para una aplicación
   */
  subirArchivo(aplicacionId: number, archivo: File): Observable<any> {
    const formData = new FormData();
    formData.append('archivo', archivo);
    return this.http.post(`${this.apiArchivos}/subir/${aplicacionId}`, formData);
  }

  /**
   * Descargar archivo instalador
   */
  descargarArchivo(nombreArchivo: string): string {
    return `${this.apiArchivos}/descargar/${nombreArchivo}`;
  }
}