import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Usuario } from '../models/usuario.model';
import { API_CONFIG } from '../config/api.config';

/**
 * Servicio para gestionar operaciones CRUD de usuarios
 */
@Injectable({
  providedIn: 'root',
})
export class UsuarioService {
  private apiUrl = `${API_CONFIG.baseUrl}${API_CONFIG.endpoints.admin.usuarios}`;

  constructor(private http: HttpClient) {}

  /**
   * Listar todos los usuarios
   */
  listarTodos(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(this.apiUrl);
  }

  /**
   * Obtener un usuario por ID
   */
  obtenerPorId(id: number): Observable<Usuario> {
    return this.http.get<Usuario>(`${this.apiUrl}/${id}`);
  }

  /**
   * Crear un nuevo usuario
   */
  crear(usuario: Usuario, password: string): Observable<Usuario> {
    const payload = { ...usuario, password };
    return this.http.post<Usuario>(this.apiUrl, payload);
  }

  /**
   * Actualizar un usuario existente
   */
  actualizar(id: number, usuario: Usuario): Observable<Usuario> {
    return this.http.put<Usuario>(`${this.apiUrl}/${id}`, usuario);
  }

  /**
   * Eliminar un usuario (borrado lógico)
   */
  eliminar(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }

  /**
   * Cambiar contraseña de un usuario
   */
  cambiarPassword(id: number, nuevoPassword: string): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}/password`, { password: nuevoPassword });
  }
}