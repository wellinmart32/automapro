/**
 * Modelo de Respuesta de Login
 */
export interface RespuestaLogin {
  token: string;
  tipo: string;
  email: string;
  nombre: string;
  rol: string;
}