/**
 * Configuración centralizada de URLs del API
 */
export const API_CONFIG = {
  baseUrl: 'http://localhost:8080',
  endpoints: {
    auth: '/api/auth',
    admin: {
      usuarios: '/api/admin/usuarios',
      aplicaciones: '/api/admin/aplicaciones',
      licencias: '/api/admin/licencias',
      archivos: '/api/admin/archivos'
    },
    cliente: {
      misApps: '/api/cliente/mis-apps',
      descargar: '/api/cliente/descargar'
    }
  }
};