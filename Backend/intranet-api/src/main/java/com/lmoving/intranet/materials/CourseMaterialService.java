	package com.lmoving.intranet.materials;

	import java.util.List;

	import org.springframework.stereotype.Service;
	import org.springframework.transaction.annotation.Transactional;

	import com.lmoving.intranet.courses.Courses;
	import com.lmoving.intranet.courses.CoursesRepository;
	import com.lmoving.intranet.materials.dto.CourseMaterialRequest;
	import com.lmoving.intranet.materials.dto.CourseMaterialResponse;

	@Service
	@Transactional
	public class CourseMaterialService {

		private final CourseMaterialRepository materialRepository;
		private final CoursesRepository coursesRepository;

		public CourseMaterialService(
				CourseMaterialRepository materialRepository,
				CoursesRepository coursesRepository
		) {
			this.materialRepository = materialRepository;
			this.coursesRepository = coursesRepository;
		}

		@Transactional(readOnly = true)
		public List<CourseMaterialResponse> getByCourse(
				Long courseId
		) {

			if (!coursesRepository.existsById(courseId)) {
				throw new RuntimeException(
						"Curso no encontrado"
				);
			}

			return materialRepository
					.findByCourseIdOrderByCreatedAtAsc(courseId)
					.stream()
					.map(this::toResponse)
					.toList();
		}

		@Transactional(readOnly = true)
		public CourseMaterialResponse getById(
				Long courseId,
				Long materialId
		) {

			CourseMaterial material =
					findMaterial(courseId, materialId);

			return toResponse(material);
		}

		public CourseMaterialResponse create(
				Long courseId,
				CourseMaterialRequest request
		) {

			Courses course =
					coursesRepository.findById(courseId)
							.orElseThrow(() ->
									new RuntimeException(
											"Curso no encontrado"
									)
							);

			CourseMaterial material =
					new CourseMaterial();

			material.setCourse(course);
			material.setTitle(request.getTitle());
			material.setDescription(
					request.getDescription()
			);
			material.setType(request.getType());
			material.setUrl(request.getUrl());
			material.setRequired(request.isRequired());
			material.setActive(request.isActive());

			CourseMaterial saved =
					materialRepository.save(material);

			return toResponse(saved);
		}

		public CourseMaterialResponse update(
				Long courseId,
				Long materialId,
				CourseMaterialRequest request
		) {

			CourseMaterial material =
					findMaterial(courseId, materialId);

			material.setTitle(request.getTitle());
			material.setDescription(
					request.getDescription()
			);
			material.setType(request.getType());
			material.setUrl(request.getUrl());
			material.setRequired(request.isRequired());
			material.setActive(request.isActive());

			return toResponse(material);
		}

		public CourseMaterialResponse toggleActive(
				Long courseId,
				Long materialId
		) {

			CourseMaterial material =
					findMaterial(courseId, materialId);

			material.setActive(
					!material.isActive()
			);

			return toResponse(material);
		}

		public void delete(
				Long courseId,
				Long materialId
		) {

			CourseMaterial material =
					findMaterial(courseId, materialId);

			materialRepository.delete(material);
		}

		private CourseMaterial findMaterial(
				Long courseId,
				Long materialId
		) {

			CourseMaterial material =
					materialRepository.findById(materialId)
							.orElseThrow(() ->
									new RuntimeException(
											"Material no encontrado"
									)
							);

			if (!material.getCourse()
					.getId()
					.equals(courseId)) {

				throw new RuntimeException(
						"El material no pertenece al curso"
				);
			}

			return material;
		}

		private CourseMaterialResponse toResponse(
				CourseMaterial material
		) {

			CourseMaterialResponse response =
					new CourseMaterialResponse();

			response.setId(material.getId());

			response.setCourseId(
					material.getCourse().getId()
			);

			response.setTitle(
					material.getTitle()
			);

			response.setDescription(
					material.getDescription()
			);

			response.setType(
					material.getType()
			);

			response.setUrl(
					material.getUrl()
			);

			response.setRequired(
					material.isRequired()
			);

			response.setActive(
					material.isActive()
			);

			response.setCreatedAt(
					material.getCreatedAt()
			);

			response.setUpdatedAt(
					material.getUpdatedAt()
			);

			return response;
		}

	@Transactional(readOnly = true)
		public CourseMaterial getEntityById(
				Long courseId,
				Long materialId
		) {

			return findMaterial(
					courseId,
					materialId
			);
	}

	@Transactional(readOnly = true)
	public CourseMaterial getEntityById(
			Long materialId
	) {

		return materialRepository.findById(materialId)
				.orElseThrow(() ->
						new RuntimeException(
								"Material no encontrado"
						)
				);
	}

}