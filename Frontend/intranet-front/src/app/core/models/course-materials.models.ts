export type MaterialType =
	| 'VIDEO'
	| 'YOUTUBE'
	| 'PDF'
	| 'PPTX'
	| 'DOCUMENT'
	| 'URL';

export interface CourseMaterial {

	id: number;

	courseId: number;

	title: string;

	description: string | null;

	type: MaterialType;

	url: string;

	required: boolean;

	active: boolean;

	createdAt: string;

	updatedAt: string;
}

export interface CourseMaterialRequest {

	title: string;

	description: string;

	type: MaterialType;

	url: string;

	required: boolean;

	active: boolean;
}

export interface CourseMaterialProgress {

	id: number;

	courseMaterialId: number;

	username: string;

	startedAt: string;

	lastAccessedAt: string;

	completedAt: string | null;

	progressPercent: number;

	lastPosition: number | null;
}

export interface CourseMaterialProgressRequest {

	progressPercent: number;

	lastPosition: number | null;
}