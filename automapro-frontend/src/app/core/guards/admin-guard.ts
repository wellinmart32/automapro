import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Auth } from '../services/auth';
import { CONSTANTES } from '../config/constantes';

/**
 * Guard para proteger rutas que requieren rol de ADMIN
 */
export const adminGuard: CanActivateFn = (route, state) => {
  const authService = inject(Auth);
  const router = inject(Router);

  // Verificar si está autenticado
  if (!authService.estaAutenticado()) {
    router.navigate(['/login']);
    return false;
  }

  // Verificar si es administrador
  const usuario = authService.getUsuarioActual();
  if (usuario?.rol === CONSTANTES.ROLES.ADMIN) {
    return true;
  }

  // Si no es admin, redirigir a una página de acceso denegado o dashboard cliente
  router.navigate(['/cliente/tablero']);
  return false;
};