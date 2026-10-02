import {
	Component,
	inject,
	OnInit
	} from '@angular/core';

import {
	FormBuilder,
	ReactiveFormsModule,
	Validators
	} from '@angular/forms';

import {
	MAT_DIALOG_DATA,
	MatDialogModule,
	MatDialogRef
	} from '@angular/material/dialog';

import {
	MatButtonModule
	} from '@angular/material/button';

import {
	CourseMaterial,
	CourseMaterialRequest,
	MaterialType
	} from '../../../core/models/course-materials.models';

@Component({
	selector: 'app-material-form-dialog',
	standalone: true,
	imports: [
		ReactiveFormsModule,
		MatDialogModule,
		MatButtonModule
	],
	templateUrl: './material-form-dialog.component.html',
	styleUrl: './material-form-dialog.component.scss'
})

export class MaterialFormDialog implements OnInit {

	private readonly fb = inject(FormBuilder);

	private readonly dialogRef =
		inject(MatDialogRef<MaterialFormDialog>);

	readonly data =
		inject(MAT_DIALOG_DATA) as CourseMaterial | null;

	readonly isEditMode =
		this.data !== null;

	materialTypes: MaterialType[] = [
		'VIDEO',
		'YOUTUBE',
		'PDF',
		'PPTX',
		'DOCUMENT',
		'URL'
	];

	selectedFile: File | null = null;

	form = this.fb.nonNullable.group({

		title: [
		this.data?.title ?? '',
		[
			Validators.required,
			Validators.maxLength(150)
		]
		],

		description: [
		this.data?.description ?? ''
		],

		type: [
		this.data?.type ?? 'VIDEO' as MaterialType,
		[
			Validators.required
		]
		],

		url: [
		this.data?.url ?? ''
		],

		required: [
		this.data?.required ?? false
		],

		active: [
		this.data?.active ?? true
		]

	});

	ngOnInit(): void {

		if (this.data) {

		this.form.patchValue({
			title: this.data.title,
			description: this.data.description ?? '',
			type: this.data.type,
			url: this.data.url,
			required: this.data.required,
			active: this.data.active
		});

		}

	}

	getTypeLabel(
		type: MaterialType
	): string {

		const labels: Record<MaterialType, string> = {

		VIDEO: 'Video',
		YOUTUBE: 'Youtube',
		PDF: 'PDF',
		PPTX: 'PPTX',
		DOCUMENT: 'Documento',
		URL: 'Enlace externo'

		};

		return labels[type];
	}

	cancel(): void {

		this.dialogRef.close();

	}

	submit(): void {

		const value = this.form.getRawValue();

		const requiresFile =
			value.type === 'VIDEO' ||
			value.type === 'PDF' ||
			value.type === 'PPTX' ||
			value.type === 'DOCUMENT';

		const requiresUrl =
			value.type === 'YOUTUBE' ||
			value.type === 'URL';

		if (requiresFile && !this.selectedFile && !this.isEditMode) {

			this.form.markAllAsTouched();

			return;
		}

		if (requiresUrl && !value.url.trim()) {

			this.form.controls.url.markAsTouched();

			return;
		}

		this.dialogRef.close({
			...value,
			file: this.selectedFile
		});
	}

	onFileSelected(event: Event): void {

		const input =
		event.target as HTMLInputElement;

		if (!input.files ||
			input.files.length === 0) {

		this.selectedFile = null;
		return;
		}

		this.selectedFile = input.files[0];
	}

	getCurrentFileName(): string {

		const url =
		this.form.controls.url.value;

		if (!url) {
		return '';
		}

		return url
		.split('/')
		.pop() ?? '';
	}

}