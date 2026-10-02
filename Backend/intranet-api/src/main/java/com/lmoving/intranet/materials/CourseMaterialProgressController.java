package com.lmoving.intranet.materials;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import com.lmoving.intranet.materials.dto.CourseMaterialProgressRequest;
import com.lmoving.intranet.materials.dto.CourseMaterialProgressResponse;

@RestController
@RequestMapping("/api/courses/{courseId}/materials/{materialId}/progress")
public class CourseMaterialProgressController {

	private final CourseMaterialProgressService progressService;

	public CourseMaterialProgressController(
			CourseMaterialProgressService progressService
	) {
		this.progressService = progressService;
	}

	@GetMapping
	public CourseMaterialProgressResponse getProgress(
			@PathVariable Long courseId,
			@PathVariable Long materialId,
			Authentication authentication
	) {

		return progressService.getProgress(
				courseId,
				materialId,
				authentication.getName()
		);
	}

	@PatchMapping
	public CourseMaterialProgressResponse updateProgress(
			@PathVariable Long courseId,
			@PathVariable Long materialId,
			@Valid @RequestBody CourseMaterialProgressRequest request,
			Authentication authentication
	) {

		return progressService.updateProgress(
				courseId,
				materialId,
				authentication.getName(),
				request
		);
	}
}