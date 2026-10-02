package com.lmoving.intranet.courses;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.lmoving.intranet.courses.courseAudience.CourseAudienceService;
import com.lmoving.intranet.courses.dto.CourseRequest;
import com.lmoving.intranet.courses.dto.CourseResponse;
import com.lmoving.intranet.user.User;
import com.lmoving.intranet.user.UserRepository;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

	private final CourseService courseService;
	private final CourseAudienceService audienceService;
	private final UserRepository userRepository;

	public CourseController(
		CourseService courseService,
		CourseAudienceService audienceService,
		UserRepository userRepository) 
	{
		this.courseService = courseService;
		this.audienceService = audienceService;
		this.userRepository = userRepository;
	}

	@GetMapping
	public List<CourseResponse> getAll() {
		return courseService.getAll();
	}

	@GetMapping("/assigned")
	public List<CourseResponse> getAssignedCourses(
			Authentication authentication
	) {

		User user = userRepository
				.findByUsername(authentication.getName())
				.orElseThrow();

		return audienceService
				.getCoursesForRole(user.getRole());
	}

	@GetMapping("/{id}")
	public CourseResponse getById(
			@PathVariable Long id) {

		return courseService.getById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public CourseResponse create(
			@Valid @RequestBody CourseRequest request) {

		return courseService.create(request);
	}

	@PutMapping("/{id}")
	public CourseResponse update(
			@PathVariable Long id,
			@Valid @RequestBody CourseRequest request) {

		return courseService.update(id, request);
	}

	@PatchMapping("/{id}/active")
	public CourseResponse toggleActive(
			@PathVariable Long id) {

		return courseService.toggleActive(id);
	}

}