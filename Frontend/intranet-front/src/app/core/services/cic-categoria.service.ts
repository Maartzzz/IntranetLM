
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { CicCategoria } from '../models/cic-categoria.models';

@Injectable({
  providedIn: 'root'
})
export class CicCategoriaService {

  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/cic';

  getCategorias(): Observable<CicCategoria[]> {
	return this.http.get<CicCategoria[]>(
	  `${this.apiUrl}/categorias`
	);
  }

  getCategoriasAdmin(): Observable<CicCategoria[]> {
	return this.http.get<CicCategoria[]>(
	  `${this.apiUrl}/admin/categorias`
	);
  }

  createCategoria(
	nombre: string,
	descripcion?: string
  ): Observable<CicCategoria> {
	return this.http.post<CicCategoria>(
	  `${this.apiUrl}/admin/categorias`,
	  { nombre, descripcion }
	);
  }

  updateCategoria(
	id: number,
	nombre: string,
	descripcion?: string
  ): Observable<CicCategoria> {
	return this.http.put<CicCategoria>(
	  `${this.apiUrl}/admin/categorias/${id}`,
	  { nombre, descripcion }
	);
  }

  toggleCategoria(
	id: number,
	activa: boolean
  ): Observable<CicCategoria> {
	return this.http.patch<CicCategoria>(
	  `${this.apiUrl}/admin/categorias/${id}/estado`,
	  { estado: activa ? 'ACTIVA' : 'INACTIVA' }
	);
  }
}
