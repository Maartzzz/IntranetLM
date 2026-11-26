package com.lmoving.intranet.materials;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import com.lmoving.intranet.materials.dto.CourseMaterialProgressRequest;
import com.lmoving.intranet.materials.dto.CourseMaterialProgressResponse;
import com.lmoving.intranet.courses.CourseAccessService;

@RestController
@RequestMapping("/api/courses/{courseId}/materials/{materialId}/progress")
public class CourseMaterialProgressController {

	private final CourseMaterialProgressService progressService;
	private final CourseAccessService courseAccessService;

	public CourseMaterialProgressController(
			CourseMaterialProgressService progressService,
			CourseAccessService courseAccessService
	) {
		this.progressService = progressService;
		this.courseAccessService = courseAccessService;
	}

    @PreAuthorize("hasAuthority('TRAINING.PROGRESS.VIEW')")
	@GetMapping
	public CourseMaterialProgressResponse getProgress(
			@PathVariable Long courseId,
			@PathVariable Long materialId,
			Authentication authentication
	) {
		courseAccessService.ensureCanAccess(courseId, authentication);
		return progressService.getProgress(
				courseId,
				materialId,
				authentication.getName()
		);
	}

    @PreAuthorize("hasAuthority('TRAINING.PROGRESS.EDIT')")
	@PatchMapping
	public CourseMaterialProgressResponse updateProgress(
			@PathVariable Long courseId,
			@PathVariable Long materialId,
			@Valid @RequestBody CourseMaterialProgressRequest request,
			Authentication authentication
	) {
		courseAccessService.ensureCanAccess(courseId, authentication);
		return progressService.updateProgress(
				courseId,
				materialId,
				authentication.getName(),
				request
		);
	}
}
