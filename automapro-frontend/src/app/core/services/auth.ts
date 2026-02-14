import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { SolicitudLogin } from '../models/solicitud-login.model';
import { RespuestaLogin } from '../models/respuesta-login.model';
import { API_CONFIG } from '../config/api.config';
import { CONSTANTES } from '../config/constantes';

/**
 * Servicio para gestionar la autenticación de usuarios
 */
@Injectable({
  providedIn: 'root',
})
export class Auth {
  private apiUrl = `${API_CONFIG.baseUrl}${API_CONFIG.endpoints.auth}`;
  private usuarioActualSubject = new BehaviorSubject<RespuestaLogin | null>(null);
  public usuarioActual$ = this.usuarioActualSubject.asObservable();

  constructor(private http: HttpClient) {
    this.cargarUsuarioDesdeStorage();
  }

  /**
   * Iniciar sesión
   */
  login(solicitud: SolicitudLogin): Observable<RespuestaLogin> {
    return this.http.post<RespuestaLogin>(`${this.apiUrl}/login`, solicitud).pipe(
      tap(respuesta => {
        localStorage.setItem(CONSTANTES.STORAGE_KEYS.TOKEN, respuesta.token);
        localStorage.setItem(CONSTANTES.STORAGE_KEYS.USUARIO, JSON.stringify(respuesta));
        this.usuarioActualSubject.next(respuesta);
      })
    );
  }

  /**
   * Cerrar sesión
   */
  logout(): void {
    localStorage.removeItem(CONSTANTES.STORAGE_KEYS.TOKEN);
    localStorage.removeItem(CONSTANTES.STORAGE_KEYS.USUARIO);
    this.usuarioActualSubject.next(null);
  }

  /**
   * Obtener el token JWT
   */
  getToken(): string | null {
    return localStorage.getItem(CONSTANTES.STORAGE_KEYS.TOKEN);
  }

  /**
   * Verificar si el usuario está autenticado
   */
  estaAutenticado(): boolean {
    return this.getToken() !== null;
  }

  /**
   * Obtener el usuario actual
   */
  getUsuarioActual(): RespuestaLogin | null {
    return this.usuarioActualSubject.value;
  }

  /**
   * Verificar si el usuario es administrador
   */
  esAdmin(): boolean {
    const usuario = this.getUsuarioActual();
    return usuario?.rol === CONSTANTES.ROLES.ADMIN;
  }

  /**
   * Cargar usuario desde localStorage al iniciar
   */
  private cargarUsuarioDesdeStorage(): void {
    const usuarioGuardado = localStorage.getItem(CONSTANTES.STORAGE_KEYS.USUARIO);
    if (usuarioGuardado) {
      this.usuarioActualSubject.next(JSON.parse(usuarioGuardado));
    }
  }
}