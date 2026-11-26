package com.lmoving.intranet.courses;

import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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

    @PreAuthorize("hasAuthority('TRAINING.COURSES.VIEW')")
	@GetMapping
	public List<CourseResponse> getAll() {
		return courseService.getAll();
	}

    @PreAuthorize("hasAuthority('TRAINING.MY_COURSES.VIEW')")
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

    @PreAuthorize("hasAnyAuthority('TRAINING.COURSES.VIEW', 'TRAINING.MY_COURSES.VIEW')")
	@GetMapping("/{id}")
	public CourseResponse getById(
			@PathVariable Long id,
			Authentication authentication) {

		boolean canViewCatalog = authentication.getAuthorities().stream()
				.anyMatch(authority -> authority.getAuthority()
						.equals("TRAINING.COURSES.VIEW"));

		if (!canViewCatalog) {
			User user = userRepository
					.findByUsername(authentication.getName())
					.orElseThrow();

			boolean assignedToUserRole = audienceService
					.getCoursesForRole(user.getRole())
					.stream()
					.anyMatch(course -> course.id().equals(id));

			if (!assignedToUserRole) {
				throw new ResponseStatusException(
						HttpStatus.NOT_FOUND,
						"Curso no encontrado"
				);
			}
		}

		return courseService.getById(id);
	}

    @PreAuthorize("hasAuthority('TRAINING.COURSES.CREATE')")
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public CourseResponse create(
			@Valid @RequestBody CourseRequest request) {

		return courseService.create(request);
	}

    @PreAuthorize("hasAuthority('TRAINING.COURSES.EDIT')")
	@PutMapping("/{id}")
	public CourseResponse update(
			@PathVariable Long id,
			@Valid @RequestBody CourseRequest request) {

		return courseService.update(id, request);
	}

    @PreAuthorize("hasAuthority('TRAINING.COURSES.DELETE')")
	@PatchMapping("/{id}/active")
	public CourseResponse toggleActive(
			@PathVariable Long id) {

		return courseService.toggleActive(id);
	}

}
