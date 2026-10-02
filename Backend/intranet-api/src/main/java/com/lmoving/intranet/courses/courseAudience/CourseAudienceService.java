package com.lmoving.intranet.courses.courseAudience;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.lmoving.intranet.courses.courseAudience.dto.CourseAudienceRequest;
import com.lmoving.intranet.courses.courseAudience.dto.CourseAudienceResponse;
import com.lmoving.intranet.courses.dto.CourseResponse;
import com.lmoving.intranet.user.Role;
import com.lmoving.intranet.courses.Courses;
import com.lmoving.intranet.courses.CoursesRepository;

@Service
public class CourseAudienceService {

	private final CourseAudienceRepository audienceRepository;
	private final CoursesRepository courseRepository;

	public CourseAudienceService(
			CourseAudienceRepository audienceRepository,
			CoursesRepository courseRepository
	) {
		this.audienceRepository = audienceRepository;
		this.courseRepository = courseRepository;
	}

	@Transactional(readOnly = true)
	public List<CourseAudienceResponse> getByCourse(
			Long courseId
	) {

		ensureCourseExists(courseId);

		return audienceRepository
				.findByCourseId(courseId)
				.stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional
	public CourseAudienceResponse add(
			Long courseId,
			CourseAudienceRequest request
	) {

		Courses course = courseRepository
				.findById(courseId)
				.orElseThrow(() ->
						new ResponseStatusException(
								HttpStatus.NOT_FOUND,
								"Curso no encontrado"
						)
				);

		Role role = request.role();

		if (audienceRepository
				.existsByCourseIdAndRole(courseId, role)) {

			throw new ResponseStatusException(
					HttpStatus.CONFLICT,
					"El rol ya está asignado al curso"
			);
		}

		CourseAudience audience =
				new CourseAudience(course, role);

		audience =
				audienceRepository.save(audience);

		return toResponse(audience);
	}

	@Transactional
	public void remove(
			Long courseId,
			Long audienceId
	) {

		CourseAudience audience =
				audienceRepository.findById(audienceId)
				.orElseThrow(() ->
						new ResponseStatusException(
								HttpStatus.NOT_FOUND,
								"Audiencia no encontrada"
						)
				);

		if (!audience.getCourse()
				.getId()
				.equals(courseId)) {

			throw new ResponseStatusException(
					HttpStatus.NOT_FOUND,
					"La audiencia no pertenece al curso"
			);
		}

		audienceRepository.delete(audience);
	}

	private void ensureCourseExists(
			Long courseId
	) {

		if (!courseRepository.existsById(courseId)) {

			throw new ResponseStatusException(
					HttpStatus.NOT_FOUND,
					"Curso no encontrado"
			);
		}
	}

	private CourseAudienceResponse toResponse(
			CourseAudience audience
	) {

		return new CourseAudienceResponse(
				audience.getId(),
				audience.getCourse().getId(),
				audience.getRole()
		);
	}

	public List<CourseResponse> getCoursesForRole(Role role) {

		return audienceRepository
				.findByRoleAndCourseActiveTrue(role)
				.stream()
				.map(CourseAudience::getCourse)
				.distinct()
				.map(course -> new CourseResponse(
						course.getId(),
						course.getTitle(),
						course.getDescription(),
						course.getDurationMinutes(),
						course.getPassingScore(),
						course.getEvaluationQuestionCount(),
						course.isActive(),
						course.getCreatedAt(),
						course.getUpdatedAt()
				))
				.toList();
	}
}