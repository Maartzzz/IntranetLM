import { Component, inject, OnInit } from '@angular/core';
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

  isAdmin(): boolean {
    return this.currentUser?.role === 'ADMIN';
  }

  isDriver(): boolean {
    return this.currentUser?.role === 'CHOFER';
  }

  isSupervisor(): boolean {
    return this.currentUser?.role === 'SUPERVISOR';
  }

}
