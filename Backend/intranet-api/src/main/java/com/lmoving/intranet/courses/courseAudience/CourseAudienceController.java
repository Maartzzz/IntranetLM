package com.lmoving.intranet.courses.courseAudience;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.lmoving.intranet.courses.courseAudience.dto.CourseAudienceRequest;
import com.lmoving.intranet.courses.courseAudience.dto.CourseAudienceResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/courses/{courseId}/audiences")
public class CourseAudienceController {

	private final CourseAudienceService audienceService;

	public CourseAudienceController(
			CourseAudienceService audienceService
	) {
		this.audienceService = audienceService;
	}

	@GetMapping
	public List<CourseAudienceResponse> getByCourse(
			@PathVariable Long courseId
	) {

		return audienceService.getByCourse(courseId);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public CourseAudienceResponse add(
			@PathVariable Long courseId,
			@Valid @RequestBody CourseAudienceRequest request
	) {

		return audienceService.add(
				courseId,
				request
		);
	}

	@DeleteMapping("/{audienceId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void remove(
			@PathVariable Long courseId,
			@PathVariable Long audienceId
	) {

		audienceService.remove(
				courseId,
				audienceId
		);
	}
}