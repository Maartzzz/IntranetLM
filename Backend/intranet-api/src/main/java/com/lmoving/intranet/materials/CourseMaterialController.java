package com.lmoving.intranet.materials;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.lmoving.intranet.materials.dto.CourseMaterialRequest;
import com.lmoving.intranet.materials.dto.CourseMaterialResponse;

@RestController
@RequestMapping("/api/courses/{courseId}/materials")
public class CourseMaterialController {

	private final CourseMaterialService materialService;

	public CourseMaterialController(
			CourseMaterialService materialService
	) {
		this.materialService = materialService;
	}

	@GetMapping
	public List<CourseMaterialResponse> getByCourse(
			@PathVariable Long courseId
	) {

		return materialService.getByCourse(courseId);
	}

	@GetMapping("/{materialId}")
	public CourseMaterialResponse getById(
			@PathVariable Long courseId,
			@PathVariable Long materialId
	) {

		return materialService.getById(
				courseId,
				materialId
		);
	}

	@PostMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
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

	@PutMapping("/{materialId}")
	@PreAuthorize("hasRole('ADMIN')")
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

	@PatchMapping("/{materialId}/active")
	@PreAuthorize("hasRole('ADMIN')")
	public CourseMaterialResponse toggleActive(
			@PathVariable Long courseId,
			@PathVariable Long materialId
	) {

		return materialService.toggleActive(
				courseId,
				materialId
		);
	}

	@DeleteMapping("/{materialId}")
	@PreAuthorize("hasRole('ADMIN')")
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