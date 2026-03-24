import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Auth } from '../services/auth';

/**
 * Guard para proteger rutas que requieren autenticación
 */
export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(Auth);
  const router = inject(Router);

  if (authService.estaAutenticado()) {
    return true;
  }

  // Guardar URL completa en localStorage para manejar query params correctamente
  localStorage.setItem('returnUrl', state.url);
  router.navigate(['/login']);
  return false;
};