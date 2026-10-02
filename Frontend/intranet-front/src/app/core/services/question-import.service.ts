import { Injectable, inject } from '@angular/core';

import {
	HttpClient
} from '@angular/common/http';

import {
	Observable
} from 'rxjs';

export interface QuestionImportResponse {
	message: string;
	rows: number;
}

@Injectable({
	providedIn: 'root'
})
export class QuestionImportService {

	private readonly http = inject(HttpClient);

	private readonly apiUrl = '/api/courses';

	importQuestions(
		courseId: number,
		file: File
	): Observable<QuestionImportResponse> {

		const formData = new FormData();

		formData.append(
			'file',
			file
		);

		return this.http.post<QuestionImportResponse>(
			`${this.apiUrl}/${courseId}/questions/import`,
			formData
		);
	}

	downloadTemplate(courseId: number): Observable<Blob> {

		return this.http.get(
			`${this.apiUrl}/${courseId}/questions/import/template`,
			{
			responseType: 'blob'
			}
		);
	}
	
}