import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth-guard';
import { adminGuard } from './core/guards/admin-guard';

export const routes: Routes = [
  // Ruta por defecto - Redirigir a catálogo público
  {
    path: '',
    redirectTo: '/catalogo',
    pathMatch: 'full'
  },

  // Ruta de catálogo público (sin autenticación)
  {
    path: 'catalogo',
    loadComponent: () => import('./features/public/catalogo/catalogo').then(m => m.Catalogo)
  },

  // Ruta de detalle de aplicación (sin autenticación)
  {
    path: 'aplicacion/:id',
    loadComponent: () => import('./features/public/detalle-aplicacion/detalle-aplicacion').then(m => m.DetalleAplicacion)
  },

  // Ruta de login (pública)
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login').then(m => m.Login)
  },

  // Ruta de registro (pública)
  {
    path: 'registro',
    loadComponent: () => import('./features/auth/registro/registro').then(m => m.Registro)
  },

  // Rutas de administrador (protegidas con authGuard y adminGuard)
  {
    path: 'admin',
    canActivate: [authGuard, adminGuard],
    children: [
      {
        path: 'tablero',
        loadComponent: () => import('./features/admin/dashboard/dashboard').then(m => m.Dashboard)
      },
      {
        path: 'usuarios',
        loadComponent: () => import('./features/admin/usuarios/usuarios').then(m => m.Usuarios)
      },
      {
        path: 'aplicaciones',
        loadComponent: () => import('./features/admin/aplicaciones/aplicaciones').then(m => m.Aplicaciones)
      },
      {
        path: 'licencias',
        loadComponent: () => import('./features/admin/licencias/licencias').then(m => m.Licencias)
      },
      {
        path: '',
        redirectTo: 'tablero',
        pathMatch: 'full'
      }
    ]
  },

  // Rutas de cliente (protegidas solo con authGuard)
  {
    path: 'cliente',
    canActivate: [authGuard],
    children: [
      {
        path: 'tablero',
        loadComponent: () => import('./features/cliente/dashboard/dashboard').then(m => m.Dashboard)
      },
      {
        path: 'mis-aplicaciones',
        loadComponent: () => import('./features/cliente/mis-apps/mis-apps').then(m => m.MisApps)
      },
      {
        path: '',
        redirectTo: 'tablero',
        pathMatch: 'full'
      }
    ]
  },

  // Ruta wildcard - Página no encontrada
  {
    path: '**',
    redirectTo: '/catalogo'
  }
];