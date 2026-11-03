import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { CicCategoria } from '../../../core/models/cic-categoria.models';
import { CicDocumento } from '../../../core/models/cic-documento.models';
import { CicCategoriaService } from '../../../core/services/cic-categoria.service';
import { CicDocumentoService } from '../../../core/services/cic-documento.service';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-cic-library',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './cic-library.component.html',
  styleUrl: './cic-library.component.scss'
})
export class CicLibraryComponent implements OnInit {

  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly categoriaService = inject(CicCategoriaService);
  private readonly documentoService = inject(CicDocumentoService);
  private readonly authService = inject(AuthService);

  categoria: CicCategoria | null = null;
  documentos: CicDocumento[] = [];

  busqueda = '';
  cargando = true;
  error = '';

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const id = Number(params.get('id'));

      if (!Number.isInteger(id) || id <= 0) {
        this.router.navigate(['/cic']);
        return;
      }

      this.cargarCategoria(id);
    });
  }

  get esAdmin(): boolean {
    return this.authService.isAdmin();
  }

  private cargarCategoria(id: number): void {
    this.cargando = true;
    this.error = '';
    this.categoria = null;
    this.documentos = [];

    this.categoriaService.getCategorias().subscribe({
      next: categorias => {
        const categoria = categorias.find(item => item.id === id);

        if (!categoria) {
          this.error = 'La categoría no existe o no está disponible.';
          this.cargando = false;
          return;
        }

        this.categoria = categoria;
        this.cargarDocumentos(id);
      },
      error: () => {
        this.error = 'No se pudo cargar la categoría.';
        this.cargando = false;
      }
    });
  }

  private cargarDocumentos(categoriaId: number): void {
    this.documentoService.getDocumentos().subscribe({
      next: documentos => {
        this.documentos = documentos.filter(
          documento => documento.categoriaId === categoriaId
        );

        this.cargando = false;
      },
      error: () => {
        this.error = 'No se pudieron cargar los documentos.';
        this.cargando = false;
      }
    });
  }

  get documentosFiltrados(): CicDocumento[] {
    const termino = this.busqueda.trim().toLocaleLowerCase();

    return this.documentos.filter(documento =>
      documento.titulo.toLocaleLowerCase().includes(termino) ||
      (documento.descripcion ?? '')
        .toLocaleLowerCase()
        .includes(termino) ||
      documento.nombreArchivo.toLocaleLowerCase().includes(termino)
    );
  }

  volverACategorias(): void {
    this.router.navigate(['/cic']);
  }

  descargar(documento: CicDocumento): void {
    this.documentoService.descargarDocumento(documento.id).subscribe({
      next: archivo => {
        const url = URL.createObjectURL(archivo);
        const enlace = document.createElement('a');

        enlace.href = url;
        enlace.download = documento.nombreArchivo;
        document.body.appendChild(enlace);
        enlace.click();
        enlace.remove();

        URL.revokeObjectURL(url);
      },
      error: () => {
        this.error = `No se pudo descargar "${documento.titulo}".`;
      }
    });
  }

  agregarDocumento(): void {
    if (this.categoria) {
      this.router.navigate(
        ['/cic/admin'],
        { queryParams: { categoriaId: this.categoria.id } }
      );
    }
  }
  
}
