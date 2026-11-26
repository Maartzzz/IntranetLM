import { Component, inject, Input, OnInit } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { CurrentUser } from '../../core/models/auth.models';

@Component({
  selector: 'app-sidebar',
  imports: [
      RouterLink,
      RouterLinkActive
  ],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss'
})
export class SidebarComponent {

  @Input() collapsed = false;

  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  
  currentUser: CurrentUser | null = null;
  
  ngOnInit(): void {
    const user = this.authService.getCurrentUser()
      .subscribe({
        next: (user) => {
          this.currentUser = user;
      }
      });
  }


  logout(): void {
    
    this.authService.logout();
    this.router.navigate(['/login']); 
  }

  hasPermission(permission: string): boolean {
    return this.authService.hasPermission(permission);
  }

  hasAnyPermission(...permissions: string[]): boolean {
    return this.authService.hasAnyPermission(...permissions);
  }

  canAccessOperations(): boolean { return this.hasPermission('OPERATIONS.VIEW'); }

  aprendizajeExpanded = false;

  toggleAprendizaje(): void {
      this.aprendizajeExpanded = !this.aprendizajeExpanded;
  }

  operacionesExpanded = false;

  toggleOperaciones(): void {
    this.operacionesExpanded = !this.operacionesExpanded;
  }

  reportesExpanded = false;

  toggleReportes(): void {
    this.reportesExpanded = !this.reportesExpanded;
  }

  configuracionExpanded = false;

  toggleConfiguracion(): void {
    this.configuracionExpanded = !this.configuracionExpanded;
  }

}
