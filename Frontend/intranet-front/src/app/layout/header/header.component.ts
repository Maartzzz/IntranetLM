import { Component, inject, EventEmitter, Output, OnInit } from '@angular/core';
import { AuthService } from '../../core/auth/auth.service';
import { CurrentUser } from '../../core/models/auth.models';

@Component({
  selector: 'app-header',
  imports: [
  ],
  templateUrl: './header.component.html',
  styleUrl: './header.component.scss'
})
export class HeaderComponent implements OnInit {

  private readonly authService = inject(AuthService);

  currentUser: CurrentUser | null = null;

  @Output()
  toggleSidebar = new EventEmitter<void>();

  toggleSidebarMenu(): void {
    this.toggleSidebar.emit();
  }

  ngOnInit(): void {
    
    const user = this.authService.getUser();
    if (user) {
      this.currentUser = user;
      return;
    }

    this.authService
      .getCurrentUser()
      .subscribe({
        next: (user) => {
          this.currentUser = user;
        },
        error: (error) => {
          console.error('Error al obtener el usuario actual:', error);
        }
      });
  }
}
