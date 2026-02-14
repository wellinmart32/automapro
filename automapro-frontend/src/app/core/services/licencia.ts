import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Licencia } from '../models/licencia.model';
import { API_CONFIG } from '../config/api.config';

/**
 * Servicio para gestionar operaciones CRUD de licencias
 */
@Injectable({
  providedIn: 'root',
})
export class LicenciaService {
  private apiUrl = `${API_CONFIG.baseUrl}${API_CONFIG.endpoints.admin.licencias}`;

  constructor(private http: HttpClient) {}

  /**
   * Listar todas las licencias
   */
  listarTodas(): Observable<Licencia[]> {
    return this.http.get<Licencia[]>(this.apiUrl);
  }

  /**
   * Listar licencias de un usuario específico
   */
  listarPorUsuario(usuarioId: number): Observable<Licencia[]> {
    return this.http.get<Licencia[]>(`${this.apiUrl}/usuario/${usuarioId}`);
  }

  /**
   * Obtener una licencia por ID
   */
  obtenerPorId(id: number): Observable<Licencia> {
    return this.http.get<Licencia>(`${this.apiUrl}/${id}`);
  }

  /**
   * Crear una nueva licencia
   */
  crear(licencia: Licencia): Observable<Licencia> {
    return this.http.post<Licencia>(this.apiUrl, licencia);
  }

  /**
   * Actualizar una licencia existente
   */
  actualizar(id: number, licencia: Licencia): Observable<Licencia> {
    return this.http.put<Licencia>(`${this.apiUrl}/${id}`, licencia);
  }

  /**
   * Eliminar una licencia (borrado lógico)
   */
  eliminar(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }
}