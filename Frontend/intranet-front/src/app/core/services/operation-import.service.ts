import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface OperationImportPreview {
  importId: number; fileName: string; sheets: string[]; sheetIndex: number; headerRow: number;
  headers: string[]; suggestedMapping: Record<string, number>; sampleRows: string[][];
}
export interface OperationImportRow {
  rowNumber: number; values: Record<string, string>; valid: boolean; possibleDuplicate: boolean; issues: string[];
}
export interface OperationImportAnalysis {
  importId: number; totalRows: number; validRows: number; duplicateRows: number; invalidRows: number; rows: OperationImportRow[];
}
export interface OperationImportProfile { id: number; name: string; mapping: Record<string, number>; updatedAt: string; }
export interface OperationImportHistory {
  id: number; fileName: string; status: string; totalRows: number; createdRows: number;
  skippedRows: number; createdAt: string; completedAt: string | null;
}

@Injectable({ providedIn: 'root' })
export class OperationImportService {
  private readonly http = inject(HttpClient);
  private readonly url = '/api/operation-imports';
  preview(file: File): Observable<OperationImportPreview> {
    const form = new FormData(); form.append('file', file, file.name);
    return this.http.post<OperationImportPreview>(`${this.url}/preview`, form);
  }
  inspect(id: number, sheetIndex: number, headerRow: number): Observable<OperationImportPreview> {
    return this.http.get<OperationImportPreview>(`${this.url}/${id}/inspect`, { params: { sheetIndex, headerRow } });
  }
  analyze(id: number, request: { sheetIndex: number; headerRow: number; mapping: Record<string, number> }): Observable<OperationImportAnalysis> {
    return this.http.post<OperationImportAnalysis>(`${this.url}/${id}/analyze`, request);
  }
  execute(id: number, request: { sheetIndex: number; headerRow: number; mapping: Record<string, number>; selectedRows: number[] }): Observable<any> {
    return this.http.post(`${this.url}/${id}/execute`, request);
  }
  getProfiles(): Observable<OperationImportProfile[]> { return this.http.get<OperationImportProfile[]>(`${this.url}/profiles`); }
  saveProfile(name: string, mapping: Record<string, number>): Observable<OperationImportProfile> {
    return this.http.post<OperationImportProfile>(`${this.url}/profiles`, { name, mapping });
  }
  deleteProfile(id: number): Observable<void> { return this.http.delete<void>(`${this.url}/profiles/${id}`); }
  getHistory(): Observable<OperationImportHistory[]> { return this.http.get<OperationImportHistory[]>(this.url); }
  downloadOriginal(id: number): Observable<Blob> { return this.http.get(`${this.url}/${id}/file`, { responseType: 'blob' }); }
}
