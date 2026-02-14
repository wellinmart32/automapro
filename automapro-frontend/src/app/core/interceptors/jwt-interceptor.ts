import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { Auth } from '../services/auth';
import { CONSTANTES } from '../config/constantes';

/**
 * Interceptor para agregar el token JWT a todas las peticiones HTTP
 */
export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(Auth);
  const router = inject(Router);
  const token = authService.getToken();

  // Clonar la petición y agregar el header de autorización si existe el token
  if (token) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  // Manejar la respuesta y capturar errores
  return next(req).pipe(
    catchError((error) => {
      // Si es error 401 (no autorizado), cerrar sesión y redirigir al login
      if (error.status === 401) {
        authService.logout();
        router.navigate(['/login']);
      }

      // Si es error 403 (sin permisos)
      if (error.status === 403) {
        console.error(CONSTANTES.MENSAJES.SIN_PERMISOS);
      }

      return throwError(() => error);
    })
  );
};