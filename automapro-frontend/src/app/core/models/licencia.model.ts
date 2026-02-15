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
  aplicacionRutaArchivo?: string; // NUEVO: Ruta del instalador
  codigo: string;
  tipoLicencia: string; // TRIAL o FULL
  fechaInicioUso?: Date;
  diasTrial?: number;
  fechaExpiracion?: Date;
  activo?: boolean;
  fechaCreacion?: Date;
}