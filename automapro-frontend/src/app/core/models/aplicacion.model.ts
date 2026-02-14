/**
 * Modelo de Aplicacion
 */
export interface Aplicacion {
  id?: number;
  nombre: string;
  descripcion?: string;
  version: string;
  rutaArchivo?: string;
  imagenUrl?: string;
  activo?: boolean;
  fechaCreacion?: Date;
}