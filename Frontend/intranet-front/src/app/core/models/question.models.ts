export interface QuestionOption {
	id?: number;
	optionText: string;
	correct: boolean;
}

export interface Question {
	id: number;
	questionText: string;
	active: boolean;
	options: QuestionOption[];
	createdAt?: string;
	updatedAt?: string;
}

export interface QuestionRequest {
	questionText: string;
	active: boolean;
	options: QuestionOptionRequest[];
}

export interface QuestionOptionRequest {
	optionText: string;
	correct: boolean;
}