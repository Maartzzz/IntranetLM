import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { Certificate } from '../../core/models/certificate.models';
import { CertificateService } from '../../core/services/certificate.service';

@Component({
  selector: 'app-certificates',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './certificates.component.html',
  styleUrl: './certificates.component.scss'
})
export class CertificatesComponent implements OnInit {

  private readonly certificateService = inject(CertificateService);

  certificates: Certificate[] = [];
  filteredCertificates: Certificate[] = [];

  searchTerm = '';

  loading = false;
  errorMessage = '';

  ngOnInit(): void {
    this.loadCertificates();
  }

  loadCertificates(): void {
    this.loading = true;
    this.errorMessage = '';

    this.certificateService.getCertificates().subscribe({
      next: (certificates) => {
        this.certificates = certificates;
        this.filteredCertificates = certificates;
        this.loading = false;
      },
      error: () => {
        this.errorMessage = 'No se pudieron cargar los certificados.';
        this.loading = false;
      }
    });
  }

  filterCertificates(): void {
    const term = this.searchTerm.trim().toLowerCase();

    if (!term) {
      this.filteredCertificates = this.certificates;
      return;
    }

    this.filteredCertificates = this.certificates.filter(certificate =>
      certificate.fullName.toLowerCase().includes(term) ||
      certificate.documentNumber.toLowerCase().includes(term) ||
      certificate.courseTitle.toLowerCase().includes(term) ||
      certificate.certificateCode.toLowerCase().includes(term)
    );
  }

  downloadCertificate(certificate: Certificate): void {
    this.certificateService
      .downloadCertificateAsAdmin(certificate.attemptId)
      .subscribe({
        next: (blob) => {
          const url = window.URL.createObjectURL(blob);
          const link = document.createElement('a');

          link.href = url;
          link.download = `${certificate.certificateCode}.pdf`;
          link.click();

          window.URL.revokeObjectURL(url);
        }
      });
  }
  
}