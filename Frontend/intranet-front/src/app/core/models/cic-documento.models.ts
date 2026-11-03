export type CicEstadoDocumento =
	| 'BORRADOR'
	| 'PUBLICADO'
	| 'ARCHIVADO';

export interface CicDocumento {
	id: number;
	titulo: string;
	descripcion: string | null;
	categoriaId: number;
	categoriaNombre: string;
	nombreArchivo: string;
	tipoMime: string;
	tamanoBytes: number;
	estado: CicEstadoDocumento;
	creadoPor: string;
	creadoEn: string;
	actualizadoEn: string;
	publicadoEn: string | null;
}

export interface CicDocumentoCreate {
	titulo: string;
	descripcion?: string;
	categoriaId: number;
	archivo: File;
}
