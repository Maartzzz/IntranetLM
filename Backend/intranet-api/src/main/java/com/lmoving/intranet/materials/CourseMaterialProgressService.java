package com.lmoving.intranet.materials;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lmoving.intranet.materials.dto.CourseMaterialProgressRequest;
import com.lmoving.intranet.materials.dto.CourseMaterialProgressResponse;
import com.lmoving.intranet.user.User;
import com.lmoving.intranet.user.UserRepository;

@Service
@Transactional
public class CourseMaterialProgressService {

	private final CourseMaterialProgressRepository progressRepository;
	private final CourseMaterialRepository materialRepository;
	private final UserRepository userRepository;

	public CourseMaterialProgressService(
			CourseMaterialProgressRepository progressRepository,
			CourseMaterialRepository materialRepository,
			UserRepository userRepository
	) {
		this.progressRepository = progressRepository;
		this.materialRepository = materialRepository;
		this.userRepository = userRepository;
	}

	public CourseMaterialProgressResponse updateProgress(
			Long courseId,
			Long materialId,
			String username,
			CourseMaterialProgressRequest request
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

		User user =
				userRepository.findByUsername(username)
						.orElseThrow(() ->
								new RuntimeException(
										"Usuario no encontrado"
								)
						);

		CourseMaterialProgress progress =
				progressRepository
						.findByCourseMaterialIdAndUserUsername(
								materialId,
								username
						)
						.orElseGet(() -> {

							CourseMaterialProgress newProgress =
									new CourseMaterialProgress();

							newProgress.setCourseMaterial(material);
							newProgress.setUser(user);

							return newProgress;
						});

					Integer progressPercent =
							request.getProgressPercent();

					if (progressPercent < 0) {
						progressPercent = 0;
					}

					if (progressPercent > 100) {
						progressPercent = 100;
					}

					Integer currentProgress =
							progress.getProgressPercent() != null
									? progress.getProgressPercent()
									: 0;

					if (progressPercent > currentProgress) {
						progress.setProgressPercent(progressPercent);
					}

					progress.setLastPosition(
							request.getLastPosition()
					);

					/*
					* Una vez completado el material,
					* no volvemos a quitar completedAt.
					*/
					if (progressPercent >= 100 &&
							progress.getCompletedAt() == null) {

						progress.setCompletedAt(
								LocalDateTime.now()
						);
					}

		CourseMaterialProgress saved =
				progressRepository.save(progress);

		return toResponse(saved);
	}

	@Transactional
	public CourseMaterialProgressResponse getProgress(
			Long courseId,
			Long materialId,
			String username
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

		CourseMaterialProgress progress =
				progressRepository
						.findByCourseMaterialIdAndUserUsername(
								materialId,
								username
						)
						.orElseGet(() -> {

							CourseMaterialProgress newProgress =
									new CourseMaterialProgress();

							newProgress.setCourseMaterial(material);
							newProgress.setUser(
									userRepository.findByUsername(username)
											.orElseThrow(() ->
													new RuntimeException(
															"Usuario no encontrado"
													)
							));

							newProgress.setProgressPercent(0);
							newProgress.setLastPosition(null);

							return progressRepository.save(newProgress);
						});

		return toResponse(progress);
	}	

	private CourseMaterialProgressResponse toResponse(
			CourseMaterialProgress progress
	) {

		CourseMaterialProgressResponse response =
				new CourseMaterialProgressResponse();

		response.setId(
				progress.getId()
		);

		response.setCourseMaterialId(
				progress.getCourseMaterial().getId()
		);

		response.setUsername(
				progress.getUser().getUsername()
		);

		response.setStartedAt(
				progress.getStartedAt()
		);

		response.setLastAccessedAt(
				progress.getLastAccessedAt()
		);

		response.setCompletedAt(
				progress.getCompletedAt()
		);

		response.setProgressPercent(
				progress.getProgressPercent()
		);

		response.setLastPosition(
				progress.getLastPosition()
		);

		return response;
	}

	@Transactional(readOnly = true)
	public boolean hasCompletedRequiredMaterials(
			Long courseId,
			String username
	) {
		return progressRepository.countIncompleteRequiredMaterials(
			courseId,
			username
		) == 0;
	}
	
}