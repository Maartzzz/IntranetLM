	package com.lmoving.intranet.materials;

	import java.util.List;

	import com.lmoving.intranet.audit.AuditLogService;

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
		private final AuditLogService auditLogService;

		public CourseMaterialService(
				CourseMaterialRepository materialRepository,
				CoursesRepository coursesRepository,
				AuditLogService auditLogService
		) {
			this.materialRepository = materialRepository;
			this.coursesRepository = coursesRepository;
			this.auditLogService = auditLogService;
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
			CourseMaterialResponse response = toResponse(saved);
			auditLogService.registrar("APRENDIZAJE", "MATERIAL", saved.getId(), "CREATED",
					"Se creó el material " + saved.getTitle() + " en el curso " + course.getTitle(), null, snapshot(saved));
			return response;
		}

		public CourseMaterialResponse update(
				Long courseId,
				Long materialId,
				CourseMaterialRequest request
		) {

			CourseMaterial material =
					findMaterial(courseId, materialId);
			String previous = snapshot(material);

			material.setTitle(request.getTitle());
			material.setDescription(
					request.getDescription()
			);
			material.setType(request.getType());
			material.setUrl(request.getUrl());
			material.setRequired(request.isRequired());
			material.setActive(request.isActive());

			CourseMaterialResponse updated = toResponse(material);
			auditLogService.registrar("APRENDIZAJE", "MATERIAL", material.getId(), "UPDATED",
					"Se actualizaron los datos del material " + material.getTitle(), previous, snapshot(material));
			return updated;
		}

		public CourseMaterialResponse toggleActive(
				Long courseId,
				Long materialId
		) {

			CourseMaterial material =
					findMaterial(courseId, materialId);

			boolean previousActive = material.isActive();
			material.setActive(!previousActive);
			CourseMaterialResponse updated = toResponse(material);
			auditLogService.registrar("APRENDIZAJE", "MATERIAL", material.getId(),
					updated.isActive() ? "ACTIVATED" : "DEACTIVATED",
					(updated.isActive() ? "Se activó el material " : "Se desactivó el material ") + material.getTitle(),
					String.valueOf(previousActive), String.valueOf(updated.isActive()));
			return updated;
		}

		public void delete(
				Long courseId,
				Long materialId
		) {

			CourseMaterial material =
					findMaterial(courseId, materialId);
			String previous = snapshot(material);
			String title = material.getTitle();
			materialRepository.delete(material);
			auditLogService.registrar("APRENDIZAJE", "MATERIAL", materialId, "DELETED",
					"Se eliminó el material " + title, previous, null);
		}

		private String snapshot(CourseMaterial material) {
			return "title=" + material.getTitle() + "; type=" + material.getType()
					+ "; required=" + material.isRequired() + "; active=" + material.isActive()
					+ "; courseId=" + material.getCourse().getId();
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