package com.lmoving.intranet.courses;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.lmoving.intranet.courses.dto.CourseRequest;
import com.lmoving.intranet.courses.dto.CourseResponse;

@Service
public class CourseService {

	private final CoursesRepository courseRepository;

	public CourseService(CoursesRepository courseRepository) {
		this.courseRepository = courseRepository;
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

		return toResponse(saved);
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

		course.setTitle(request.title());
		course.setDescription(request.description());
		course.setDurationMinutes(request.durationMinutes());
		course.setPassingScore(request.passingScore());
		course.setEvaluationQuestionCount(request.evaluationQuestionCount());

		if (request.active() != null) {
			course.setActive(request.active());
		}

		return toResponse(course);
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

		course.setActive(!course.isActive());

		return toResponse(course);
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