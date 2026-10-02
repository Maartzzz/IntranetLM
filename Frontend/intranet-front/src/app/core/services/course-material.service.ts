import { Injectable, inject } from '@angular/core';

import {
	HttpClient
	} from '@angular/common/http';

import {
	Observable
	} from 'rxjs';

import {
	CourseMaterial,
	CourseMaterialRequest,
	CourseMaterialProgress,
	CourseMaterialProgressRequest
	} from '../models/course-materials.models';

	interface MaterialUploadResponse {
		filename: string;
		url: string;
	}

	@Injectable({
	providedIn: 'root'
	})
	export class CourseMaterialService {

	private readonly http = inject(HttpClient);

	private readonly apiUrl = '/api/courses';

	getByCourse(
		courseId: number
	): Observable<CourseMaterial[]> {

		return this.http.get<CourseMaterial[]>(
		`${this.apiUrl}/${courseId}/materials`
		);
	}

	getById(
		courseId: number,
		materialId: number
	): Observable<CourseMaterial> {

		return this.http.get<CourseMaterial>(
		`${this.apiUrl}/${courseId}/materials/${materialId}`
		);
	}

	create(
		courseId: number,
		request: CourseMaterialRequest
	): Observable<CourseMaterial> {

		return this.http.post<CourseMaterial>(
		`${this.apiUrl}/${courseId}/materials`,
		request
		);
	}

	update(
		courseId: number,
		materialId: number,
		request: CourseMaterialRequest
	): Observable<CourseMaterial> {

		return this.http.put<CourseMaterial>(
		`${this.apiUrl}/${courseId}/materials/${materialId}`,
		request
		);
	}

	toggleActive(
		courseId: number,
		materialId: number
	): Observable<CourseMaterial> {

		return this.http.patch<CourseMaterial>(
		`${this.apiUrl}/${courseId}/materials/${materialId}/active`,
		{}
		);
	}

	delete(
		courseId: number,
		materialId: number
	): Observable<void> {

		return this.http.delete<void>(
		`${this.apiUrl}/${courseId}/materials/${materialId}`
		);
	}

	getProgress(
		courseId: number,
		materialId: number
	): Observable<CourseMaterialProgress> {

		return this.http.get<CourseMaterialProgress>(
		`${this.apiUrl}/${courseId}/materials/${materialId}/progress`
		);
	}

	updateProgress(
		courseId: number,
		materialId: number,
		request: CourseMaterialProgressRequest
	): Observable<CourseMaterialProgress> {

		return this.http.patch<CourseMaterialProgress>(
		`${this.apiUrl}/${courseId}/materials/${materialId}/progress`,
		request
		);
	}

	upload(
	file: File
	): Observable<{
	filename: string;
	url: string;
	}> {

		const formData = new FormData();

		formData.append(
			'file',
			file
		);

		return this.http.post<{
			filename: string;
			url: string;
		}>(
			'/api/materials/upload',
			formData
		);
	}

	getContent(
	materialId: number
	): Observable<Blob> {

		return this.http.get(
			`/api/materials/${materialId}/content`,
			{
			responseType: 'blob'
			}
		);
	}

}