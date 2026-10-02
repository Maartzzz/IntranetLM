import { Injectable, inject
} from '@angular/core';

import { HttpClient
} from '@angular/common/http';

import { Observable
} from 'rxjs';

import { Question, QuestionRequest
} from '../models/question.models';

@Injectable({
providedIn: 'root'
})
export class QuestionService {

	private readonly http = inject(HttpClient);

	private readonly apiUrl = '/api/questions';

	getQuestions(): Observable<Question[]> {
		return this.http.get<Question[]>(
		this.apiUrl
		);
	}

	getQuestion(id: number): Observable<Question> {
		return this.http.get<Question>(
		`${this.apiUrl}/${id}`
		);
	}

	createQuestion(
		request: QuestionRequest
	): Observable<Question> {
		return this.http.post<Question>(
		this.apiUrl,
		request
		);
	}

	updateQuestion(
		id: number,
		request: QuestionRequest
	): Observable<Question> {
		return this.http.put<Question>(
		`${this.apiUrl}/${id}`,
		request
		);
	}

	toggleActive(
		id: number
	): Observable<Question> {
		return this.http.patch<Question>(
		`${this.apiUrl}/${id}/toggle-active`,
		{}
		);
	}

	deleteQuestion(
		id: number
	): Observable<void> {
		return this.http.delete<void>(
		`${this.apiUrl}/${id}`
		);
	}
	
}