package com.lmoving.intranet.courses;

import java.util.List;

import com.lmoving.intranet.audit.AuditLogService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.lmoving.intranet.courses.dto.CourseRequest;
import com.lmoving.intranet.courses.dto.CourseResponse;

@Service
public class CourseService {

	private final CoursesRepository courseRepository;
	private final AuditLogService auditLogService;

	public CourseService(CoursesRepository courseRepository, AuditLogService auditLogService) {
		this.courseRepository = courseRepository;
		this.auditLogService = auditLogService;
	}

	@Transactional(readOnly = true)
	public List<CourseResponse> getAll() {

		return courseRepository.findAll()
				.stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public CourseResponse getById(Long id) {

		Courses course = courseRepository.findById(id)
				.orElseThrow(() ->
						new ResponseStatusException(
								HttpStatus.NOT_FOUND,
								"Curso no encontrado"
						)
				);

		return toResponse(course);
	}

	@Transactional 
	public CourseResponse create(CourseRequest request) {

		Courses course = new Courses();

		course.setTitle(request.title());
		course.setDescription(request.description());
		course.setDurationMinutes(request.durationMinutes());
		course.setPassingScore(request.passingScore());
		course.setEvaluationQuestionCount(request.evaluationQuestionCount());

		course.setActive(
				request.active() == null
						|| request.active()
		);

		Courses saved =
				courseRepository.save(course);
		CourseResponse response = toResponse(saved);
			auditLogService.registrar("APRENDIZAJE", "CURSO", saved.getId(), "CREATED",
				"Se creó el curso " + saved.getTitle(), null, response.toString());
		return response;
	}
	
	@Transactional
	public CourseResponse update(
			Long id,
			CourseRequest request
	) {

		Courses course = courseRepository.findById(id)
				.orElseThrow(() ->
						new ResponseStatusException(
								HttpStatus.NOT_FOUND,
								"Curso no encontrado"
						)
				);
		CourseResponse previous = toResponse(course);

		course.setTitle(request.title());
		course.setDescription(request.description());
		course.setDurationMinutes(request.durationMinutes());
		course.setPassingScore(request.passingScore());
		course.setEvaluationQuestionCount(request.evaluationQuestionCount());

		if (request.active() != null) {
			course.setActive(request.active());
		}

		CourseResponse updated = toResponse(course);
		auditLogService.registrar("APRENDIZAJE", "CURSO", course.getId(), "UPDATED",
			"Se actualizaron los datos del curso " + course.getTitle(), previous.toString(), updated.toString());
		return updated;
	}

	@Transactional
	public CourseResponse toggleActive(Long id) {

		Courses course = courseRepository.findById(id)
				.orElseThrow(() ->
						new ResponseStatusException(
								HttpStatus.NOT_FOUND,
								"Curso no encontrado"
						)
				);

		boolean previousActive = course.isActive();
		course.setActive(!previousActive);
		CourseResponse updated = toResponse(course);
		auditLogService.registrar("APRENDIZAJE", "CURSO", course.getId(),
				updated.active() ? "ACTIVATED" : "DEACTIVATED",
				(updated.active() ? "Se activó el curso " : "Se desactivó el curso ") + course.getTitle(),
				String.valueOf(previousActive), String.valueOf(updated.active()));
		return updated;
	}

	private CourseResponse toResponse(
			Courses course
	) {

		return new CourseResponse(
				course.getId(),
				course.getTitle(),
				course.getDescription(),
				course.getDurationMinutes(),
				course.getPassingScore(),
				course.getEvaluationQuestionCount(),
				course.isActive(),
				course.getCreatedAt(),
				course.getUpdatedAt()
		);
	}
}