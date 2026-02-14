/**
 * Modelo de Licencia
 */
export interface Licencia {
  id?: number;
  usuarioId: number;
  usuarioNombre?: string;
  usuarioEmail?: string;
  aplicacionId: number;
  aplicacionNombre?: string;
  aplicacionVersion?: string;
  codigo: string;
  fechaExpiracion?: Date;
  activo?: boolean;
  fechaCreacion?: Date;
}