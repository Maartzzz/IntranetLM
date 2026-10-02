import { Injectable, inject
} from '@angular/core';

import { HttpClient
} from '@angular/common/http';

import { Observable
} from 'rxjs';

import { Question
} from '../models/question.models';

@Injectable({ providedIn: 'root'
})
export class CourseQuestionsService {

	private readonly http = inject(HttpClient);

	private readonly apiUrl = '/api/courses';

	getQuestionsByCourse(
		courseId: number
	): Observable<Question[]> {

		return this.http.get<Question[]>(
		`${this.apiUrl}/${courseId}/questions`
		);
	}

	addQuestionToCourse(
		courseId: number,
		questionId: number
	): Observable<void> {

		return this.http.post<void>(
		`${this.apiUrl}/${courseId}/questions/${questionId}`,
		{}
		);
	}

	removeQuestionFromCourse(
		courseId: number,
		questionId: number
	): Observable<void> {

		return this.http.delete<void>(
		`${this.apiUrl}/${courseId}/questions/${questionId}`
		);
	}
	
}