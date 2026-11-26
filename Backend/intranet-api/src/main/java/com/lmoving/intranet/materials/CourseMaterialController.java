package com.lmoving.intranet.materials;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.lmoving.intranet.materials.dto.CourseMaterialRequest;
import com.lmoving.intranet.materials.dto.CourseMaterialResponse;
import com.lmoving.intranet.courses.CourseAccessService;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/courses/{courseId}/materials")
public class CourseMaterialController {

	private final CourseMaterialService materialService;
	private final CourseAccessService courseAccessService;

	public CourseMaterialController(
			CourseMaterialService materialService,
			CourseAccessService courseAccessService
	) {
		this.materialService = materialService;
		this.courseAccessService = courseAccessService;
	}

    @PreAuthorize("hasAuthority('TRAINING.MATERIALS.VIEW')")
	@GetMapping
	public List<CourseMaterialResponse> getByCourse(
			@PathVariable Long courseId,
			Authentication authentication
	) {
		courseAccessService.ensureCanAccess(courseId, authentication);
		return materialService.getByCourse(courseId);
	}

    @PreAuthorize("hasAuthority('TRAINING.MATERIALS.VIEW')")
	@GetMapping("/{materialId}")
	public CourseMaterialResponse getById(
			@PathVariable Long courseId,
			@PathVariable Long materialId,
			Authentication authentication
	) {
		courseAccessService.ensureCanAccess(courseId, authentication);
		return materialService.getById(
				courseId,
				materialId
		);
	}

    @PreAuthorize("hasAuthority('TRAINING.MATERIALS.CREATE')")
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public CourseMaterialResponse create(
			@PathVariable Long courseId,
			@Valid @RequestBody CourseMaterialRequest request
	) {

		return materialService.create(
				courseId,
				request
		);
	}

    @PreAuthorize("hasAuthority('TRAINING.MATERIALS.EDIT')")
	@PutMapping("/{materialId}")
	public CourseMaterialResponse update(
			@PathVariable Long courseId,
			@PathVariable Long materialId,
			@Valid @RequestBody CourseMaterialRequest request
	) {

		return materialService.update(
				courseId,
				materialId,
				request
		);
	}

    @PreAuthorize("hasAuthority('TRAINING.MATERIALS.EDIT')")
	@PatchMapping("/{materialId}/active")
	public CourseMaterialResponse toggleActive(
			@PathVariable Long courseId,
			@PathVariable Long materialId
	) {

		return materialService.toggleActive(
				courseId,
				materialId
		);
	}

    @PreAuthorize("hasAuthority('TRAINING.MATERIALS.DELETE')")
	@DeleteMapping("/{materialId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(
			@PathVariable Long courseId,
			@PathVariable Long materialId
	) {

		materialService.delete(
				courseId,
				materialId
		);
	}
}
