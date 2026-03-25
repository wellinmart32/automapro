import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { Auth } from '../services/auth';
import { CONSTANTES } from '../config/constantes';

/**
 * Interceptor para agregar el token JWT a todas las peticiones HTTP
 * y manejar sesiones expiradas redirigiendo al login
 */
export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(Auth);
  const router = inject(Router);
  const token = authService.getToken();

  if (token) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(req).pipe(
    catchError((error) => {
      if (error.status === 401) {
        // Guardar URL actual para redirigir después del login
        const urlActual = router.url;
        if (urlActual && urlActual !== '/login') {
          localStorage.setItem('returnUrl', urlActual);
        }
        authService.logout();
        router.navigate(['/login'], {
          queryParams: { sesionExpirada: 'true' }
        });
      }

      if (error.status === 403) {
        console.error(CONSTANTES.MENSAJES.SIN_PERMISOS);
      }

      return throwError(() => error);
    })
  );
};