import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { OperationImportAnalysis, OperationImportHistory, OperationImportPreview, OperationImportProfile, OperationImportService } from '../../../core/services/operation-import.service';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-operation-import', standalone: true, imports: [CommonModule, FormsModule],
  templateUrl: './operation-import.component.html', styleUrl: './operation-import.component.scss'
})
export class OperationImportComponent implements OnInit {
  private readonly api = inject(OperationImportService);
  private readonly authService = inject(AuthService);

  get canImport(): boolean { return this.authService.hasPermission('OPERATIONS.IMPORT'); }
  get canViewHistory(): boolean { return this.authService.hasPermission('OPERATIONS.IMPORT_HISTORY_VIEW'); }
  get canViewProfiles(): boolean { return this.authService.hasPermission('OPERATIONS.IMPORT_PROFILE_VIEW'); }
  get canManageProfiles(): boolean { return this.authService.hasPermission('OPERATIONS.IMPORT_PROFILE_MANAGE'); }
  readonly fields = [
    { key: 'reference', label: 'Referencia', required: false },
    { key: 'client', label: 'Cliente', required: true },
    { key: 'cargoType', label: 'Tipo de carga', required: true },
    { key: 'customerOrderNumber', label: 'Pedido del cliente', required: false },
    { key: 'origin', label: 'Origen', required: false },
    { key: 'destination', label: 'Destino', required: false },
    { key: 'quantity', label: 'Cantidad (contenedores / bultos)', required: false },
    { key: 'estimatedDate', label: 'Fecha estimada', required: false },
    { key: 'observation', label: 'Observación', required: false }
  ];
  preview: OperationImportPreview | null = null;
  analysis: OperationImportAnalysis | null = null;
  profiles: OperationImportProfile[] = [];
  history: OperationImportHistory[] = [];
  mapping: Record<string, number | null> = {};
  selectedRows = new Set<number>();
  selectedFile: File | null = null;
  selectedProfileId: number | null = null;
  profileName = '';
  sheetIndex = 0;
  headerRowInput = 1;
  loading = false;
  executing = false;
  errorMessage = '';
  successMessage = '';

  ngOnInit(): void { this.loadReferenceData(); }
  get mappingReady(): boolean {
    return this.mapping['client'] !== null && this.mapping['client'] !== undefined &&
      this.mapping['cargoType'] !== null && this.mapping['cargoType'] !== undefined;
  }
  loadReferenceData(): void {
    if (this.canViewProfiles || this.canManageProfiles) {
      this.api.getProfiles().subscribe({ next: rows => this.profiles = rows, error: () => this.profiles = [] });
    }
    if (this.canViewHistory) {
      this.api.getHistory().subscribe({ next: rows => this.history = rows, error: () => this.history = [] });
    }
  }
  onFileChange(event: Event): void {
    if (!this.canImport) return;
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    if (!file) return;
    const extension = file.name.toLowerCase().split('.').pop();
    if (extension !== 'xls' && extension !== 'xlsx') { this.errorMessage = 'Selecciona un archivo .xls o .xlsx.'; input.value = ''; return; }
    this.selectedFile = file; this.preview = null; this.analysis = null;
    this.errorMessage = ''; this.successMessage = ''; this.loading = true;
    this.api.preview(file).subscribe({
      next: result => {
        this.preview = result; this.sheetIndex = result.sheetIndex; this.headerRowInput = result.headerRow + 1;
        this.mapping = {};
        for (const field of this.fields) this.mapping[field.key] = result.suggestedMapping[field.key] ?? null;
        this.analysis = null; this.selectedRows.clear(); this.loading = false; this.loadReferenceData();
      },
      error: error => { this.loading = false; this.errorMessage = this.messageFrom(error, 'No se pudo leer el archivo.'); }
    });
  }
  changeSheet(): void { this.refreshPreview(); }
  changeHeaderRow(): void { this.refreshPreview(); }
  private refreshPreview(): void {
    if (!this.preview) return;
    this.loading = true; this.analysis = null; this.errorMessage = '';
    this.api.inspect(this.preview.importId, this.sheetIndex, Math.max(0, this.headerRowInput - 1)).subscribe({
      next: result => {
        this.preview = result; this.mapping = {};
        for (const field of this.fields) this.mapping[field.key] = result.suggestedMapping[field.key] ?? null;
        this.loading = false;
      },
      error: error => { this.loading = false; this.errorMessage = this.messageFrom(error, 'No se pudo inspeccionar la hoja.'); }
    });
  }
  analyze(): void {
    if (!this.canImport) return;
    if (!this.preview || !this.mappingReady || this.loading) return;
    this.loading = true; this.errorMessage = ''; this.successMessage = ''; this.analysis = null;
    this.api.analyze(this.preview.importId, {
      sheetIndex: this.sheetIndex, headerRow: Math.max(0, this.headerRowInput - 1), mapping: this.cleanMapping()
    }).subscribe({
      next: result => {
        this.analysis = result;
        this.selectedRows = new Set(result.rows.filter(row => row.valid && !row.possibleDuplicate).map(row => row.rowNumber));
        this.loading = false;
      },
      error: error => { this.loading = false; this.errorMessage = this.messageFrom(error, 'No se pudo validar el mapeo.'); }
    });
  }
  toggleRow(rowNumber: number, checked: boolean): void { if (checked) this.selectedRows.add(rowNumber); else this.selectedRows.delete(rowNumber); }
  isSelected(rowNumber: number): boolean { return this.selectedRows.has(rowNumber); }
  selectAllValid(): void { if (this.analysis) this.selectedRows = new Set(this.analysis.rows.filter(row => row.valid).map(row => row.rowNumber)); }
  selectRecommended(): void { if (this.analysis) this.selectedRows = new Set(this.analysis.rows.filter(row => row.valid && !row.possibleDuplicate).map(row => row.rowNumber)); }
  execute(): void {
    if (!this.canImport) return;
    if (!this.preview || !this.analysis || this.selectedRows.size === 0 || this.executing) return;
    this.executing = true; this.errorMessage = ''; this.successMessage = '';
    this.api.execute(this.preview.importId, {
      sheetIndex: this.sheetIndex, headerRow: Math.max(0, this.headerRowInput - 1),
      mapping: this.cleanMapping(), selectedRows: [...this.selectedRows]
    }).subscribe({
      next: result => {
        this.executing = false;
        this.successMessage = `Importación finalizada. Se crearon ${result.createdRows} operaciones y se omitieron ${result.skippedRows}.`;
        this.loadReferenceData(); this.analysis = null; this.preview = null; this.selectedFile = null; this.selectedRows.clear();
      },
      error: error => { this.executing = false; this.errorMessage = this.messageFrom(error, 'No se pudo completar la importación.'); }
    });
  }
  saveProfile(): void {
    if (!this.canManageProfiles) return;
    if (!this.profileName.trim() || !this.preview) return;
    this.api.saveProfile(this.profileName.trim(), this.cleanMapping()).subscribe({
      next: profile => {
        this.profiles = [...this.profiles.filter(item => item.id !== profile.id), profile].sort((a, b) => a.name.localeCompare(b.name));
        this.profileName = ''; this.successMessage = `Se guardó el perfil "${profile.name}".`;
      }, error: error => this.errorMessage = this.messageFrom(error, 'No se pudo guardar el perfil.')
    });
  }
  applyProfile(): void {
    if (!this.canViewProfiles && !this.canManageProfiles) return;
    const profile = this.profiles.find(item => item.id === this.selectedProfileId);
    if (!profile || !this.preview) return;
    this.mapping = {};
    for (const field of this.fields) {
      const index = profile.mapping[field.key];
      this.mapping[field.key] = Number.isInteger(index) && index >= 0 && index < this.preview.headers.length ? index : null;
    }
    this.analysis = null;
  }
  deleteProfile(profile: OperationImportProfile): void {
    if (!this.canManageProfiles) return;
    this.api.deleteProfile(profile.id).subscribe({
      next: () => this.profiles = this.profiles.filter(item => item.id !== profile.id),
      error: error => this.errorMessage = this.messageFrom(error, 'No se pudo eliminar el perfil.')
    });
  }
  downloadOriginal(item: OperationImportHistory): void {
    if (!this.canViewHistory) return;
    this.api.downloadOriginal(item.id).subscribe({
      next: blob => {
        const url = URL.createObjectURL(blob); const anchor = document.createElement('a');
        anchor.href = url; anchor.download = item.fileName; anchor.click(); URL.revokeObjectURL(url);
      }, error: error => this.errorMessage = this.messageFrom(error, 'No se pudo descargar el archivo original.')
    });
  }
  reset(): void {
    this.preview = null; this.analysis = null; this.selectedFile = null; this.selectedRows.clear();
    this.mapping = {}; this.errorMessage = ''; this.successMessage = '';
  }
  private cleanMapping(): Record<string, number> {
    const result: Record<string, number> = {};
    for (const field of this.fields) {
      const value = this.mapping[field.key]; if (value !== null && value !== undefined) result[field.key] = Number(value);
    }
    return result;
  }
  private messageFrom(error: any, fallback: string): string {
    return error?.error?.message || error?.error?.detail || error?.message || fallback;
  }
}
