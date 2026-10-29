import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { CicCategoria } from '../../../core/models/cic-categoria.models';
import { CicDocumento } from '../../../core/models/cic-documento.models';
import { CicCategoriaService } from '../../../core/services/cic-categoria.service';
import { CicDocumentoService } from '../../../core/services/cic-documento.service';

@Component({
  selector: 'app-cic-admin',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './cic-admin.component.html',
  styleUrl: './cic-admin.component.scss'
})
export class CicAdminComponent implements OnInit {

  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly categoriaService = inject(CicCategoriaService);
  private readonly documentoService = inject(CicDocumentoService);

  categorias: CicCategoria[] = [];
  documentos: CicDocumento[] = [];

  seccion: 'categorias' | 'documentos' = 'documentos';
  categoriaFiltro: number | null = null;

  nombreCategoria = '';
  descripcionCategoria = '';
  categoriaEditandoId: number | null = null;

  tituloDocumento = '';
  descripcionDocumento = '';
  categoriaDocumentoId: number | null = null;
  archivoSeleccionado: File | null = null;

  guardando = false;
  cargando = true;
  mensaje = '';
  error = '';

  ngOnInit(): void {
    const categoriaId = Number(
      this.route.snapshot.queryParamMap.get('categoriaId')
    );

    if (Number.isInteger(categoriaId) && categoriaId > 0) {
      this.categoriaDocumentoId = categoriaId;
      this.categoriaFiltro = categoriaId;
      this.seccion = 'documentos';
    }

    this.cargarCategorias();
    this.cargarDocumentos();
  }

  cargarCategorias(): void {
    this.categoriaService.getCategoriasAdmin().subscribe({
      next: categorias => {
        this.categorias = categorias;
      },
      error: () => {
        this.error = 'No se pudieron cargar las categorías.';
      }
    });
  }

  cargarDocumentos(): void {
    this.cargando = true;

    this.documentoService.getDocumentosAdmin().subscribe({
      next: documentos => {
        this.documentos = documentos;
        this.cargando = false;
      },
      error: () => {
        this.error = 'No se pudieron cargar los documentos.';
        this.cargando = false;
      }
    });
  }

  get documentosFiltrados(): CicDocumento[] {
    return this.documentos.filter(documento =>
      this.categoriaFiltro === null ||
      documento.categoriaId === this.categoriaFiltro
    );
  }

  seleccionarSeccion(seccion: 'categorias' | 'documentos'): void {
    this.seccion = seccion;
    this.mensaje = '';
    this.error = '';
  }

  guardarCategoria(): void {
    this.mensaje = '';
    this.error = '';

    if (!this.nombreCategoria.trim()) {
      this.error = 'El nombre de la categoría es obligatorio.';
      return;
    }

    this.guardando = true;

    const operacion = this.categoriaEditandoId === null
      ? this.categoriaService.createCategoria(
          this.nombreCategoria.trim(),
          this.descripcionCategoria.trim()
        )
      : this.categoriaService.updateCategoria(
          this.categoriaEditandoId,
          this.nombreCategoria.trim(),
          this.descripcionCategoria.trim()
        );

    operacion.subscribe({
      next: () => {
        this.mensaje = this.categoriaEditandoId === null
          ? 'Categoría creada correctamente.'
          : 'Categoría actualizada correctamente.';

        this.limpiarFormularioCategoria();
        this.cargarCategorias();
        this.guardando = false;
      },
      error: err => {
        this.error = err.error?.message
          || 'No se pudo guardar la categoría.';
        this.guardando = false;
      }
    });
  }

  editarCategoria(categoria: CicCategoria): void {
    this.categoriaEditandoId = categoria.id;
    this.nombreCategoria = categoria.nombre;
    this.descripcionCategoria = categoria.descripcion ?? '';
    this.mensaje = '';
    this.error = '';
    this.seccion = 'categorias';
  }

  cambiarEstadoCategoria(categoria: CicCategoria): void {
    const nuevoEstado = !categoria.activa;

    this.categoriaService
      .toggleCategoria(categoria.id, nuevoEstado)
      .subscribe({
        next: () => {
          this.mensaje = 'Estado de categoría actualizado.';
          this.cargarCategorias();
        },
        error: () => {
          this.error = 'No se pudo cambiar el estado de la categoría.';
        }
      });
  }

  limpiarFormularioCategoria(): void {
    this.nombreCategoria = '';
    this.descripcionCategoria = '';
    this.categoriaEditandoId = null;
  }

  seleccionarArchivo(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.archivoSeleccionado = input.files?.[0] ?? null;
  }

  subirDocumento(): void {
    this.mensaje = '';
    this.error = '';

    if (!this.tituloDocumento.trim()) {
      this.error = 'El título del documento es obligatorio.';
      return;
    }

    if (this.categoriaDocumentoId === null) {
      this.error = 'Selecciona una categoría.';
      return;
    }

    if (!this.archivoSeleccionado) {
      this.error = 'Selecciona un archivo para subir.';
      return;
    }

    this.guardando = true;

    this.documentoService.createDocumento(
      this.tituloDocumento.trim(),
      this.categoriaDocumentoId,
      this.archivoSeleccionado,
      this.descripcionDocumento.trim()
    ).subscribe({
      next: () => {
        this.mensaje =
          'Documento cargado como borrador. Puedes publicarlo desde el listado.';

        this.tituloDocumento = '';
        this.descripcionDocumento = '';
        this.archivoSeleccionado = null;

        this.cargarDocumentos();
        this.guardando = false;
      },
      error: err => {
        this.error = err.error?.message
          || 'No se pudo subir el documento.';
        this.guardando = false;
      }
    });
  }

  cambiarEstadoDocumento(
    documento: CicDocumento,
    estado: 'BORRADOR' | 'PUBLICADO' | 'ARCHIVADO'
  ): void {
    this.mensaje = '';
    this.error = '';

    this.documentoService
      .cambiarEstado(documento.id, estado)
      .subscribe({
        next: () => {
          this.mensaje = 'Estado del documento actualizado.';
          this.cargarDocumentos();
        },
        error: err => {
          this.error = err.error?.message
            || 'No se pudo cambiar el estado del documento.';
        }
      });
  }

  volverAlCic(): void {
    this.router.navigate(['/cic']);
  }
}
