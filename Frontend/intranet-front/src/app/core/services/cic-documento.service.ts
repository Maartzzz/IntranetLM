
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { CicDocumento } from '../models/cic-documento.models';

@Injectable({
  providedIn: 'root'
})
export class CicDocumentoService {

  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/cic';

  getDocumentos(): Observable<CicDocumento[]> {
	return this.http.get<CicDocumento[]>(
	  `${this.apiUrl}/documentos`
	);
  }

  getDocumento(id: number): Observable<CicDocumento> {
	return this.http.get<CicDocumento>(
	  `${this.apiUrl}/documentos/${id}`
	);
  }

  getDocumentosAdmin(): Observable<CicDocumento[]> {
	return this.http.get<CicDocumento[]>(
	  `${this.apiUrl}/admin/documentos`
	);
  }

  createDocumento(
	titulo: string,
	categoriaId: number,
	archivo: File,
	descripcion?: string
  ): Observable<CicDocumento> {

	const formData = new FormData();

	formData.append('titulo', titulo);
	formData.append('categoriaId', categoriaId.toString());
	formData.append('archivo', archivo);

	if (descripcion?.trim()) {
	  formData.append('descripcion', descripcion.trim());
	}

	return this.http.post<CicDocumento>(
	  `${this.apiUrl}/admin/documentos`,
	  formData
	);
  }

  cambiarEstado(
	id: number,
	estado: 'BORRADOR' | 'PUBLICADO' | 'ARCHIVADO'
  ): Observable<CicDocumento> {
	return this.http.patch<CicDocumento>(
	  `${this.apiUrl}/admin/documentos/${id}/estado`,
	  { estado }
	);
  }

  descargarDocumento(id: number): Observable<Blob> {
	return this.http.get(
	  `${this.apiUrl}/documentos/${id}/archivo`,
	  { responseType: 'blob' }
	);
  }
}
