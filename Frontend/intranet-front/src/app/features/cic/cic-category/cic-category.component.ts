
import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { Router } from '@angular/router';

import { CicCategoria } from '../../../core/models/cic-categoria.models';
import { CicCategoriaService } from '../../../core/services/cic-categoria.service';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-cic-category',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './cic-category.component.html',
  styleUrl: './cic-category.component.scss'
})
export class CicCategoryComponent implements OnInit {

  private readonly categoriaService = inject(CicCategoriaService);
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);

  categorias: CicCategoria[] = [];
  cargando = true;
  error = '';

  private readonly descripciones: Record<string, string> = {
    'Operaciones': 'Procedimientos y documentación operativa.',
    'Seguridad y salud': 'Prevención de riesgos y seguridad laboral.',
    'Políticas y reglamentos': 'Normas y disposiciones internas.',
    'Administración': 'Información y documentos administrativos.',
    'Información institucional': 'Información general de la empresa.',
    'Formatos y fichas': 'Plantillas, formatos y fichas descargables.'
  };

  ngOnInit(): void {
    this.categoriaService.getCategorias().subscribe({
      next: categorias => {
        this.categorias = categorias;
        this.cargando = false;
      },
      error: () => {
        this.error = 'No se pudieron cargar las categorías.';
        this.cargando = false;
      }
    });
  }
  get esAdmin(): boolean {
    return this.authService.isAdmin();
  }

  obtenerDescripcion(nombre: string): string {
    return this.descripciones[nombre]
      ?? 'Consulta los documentos disponibles en esta categoría.';
  }

  abrirCategoria(categoria: CicCategoria): void {
    this.router.navigate(['/cic/category', categoria.id]);
  }

  trackById(_: number, categoria: CicCategoria): number {
    return categoria.id;
  }

  irAAdministrarCategorias(): void {
    this.router.navigate(['/cic/admin']);
  }
  
}
