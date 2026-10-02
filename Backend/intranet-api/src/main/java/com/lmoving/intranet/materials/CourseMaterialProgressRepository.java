package com.lmoving.intranet.materials;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseMaterialProgressRepository
		extends JpaRepository<CourseMaterialProgress, Long> {

	Optional<CourseMaterialProgress>
	findByCourseMaterialIdAndUserUsername(
			Long courseMaterialId,
			String username
	);

	@Query("""
		SELECT COUNT(cm)
		FROM CourseMaterial cm
		WHERE cm.course.id = :courseId
			AND cm.required = true
			AND (
				cm.active = true
				AND NOT EXISTS (
					SELECT p.id
					FROM CourseMaterialProgress p
					WHERE p.courseMaterial.id = cm.id
						AND p.user.username = :username
						AND p.progressPercent = 100
				)
			)
	""")
	long countIncompleteRequiredMaterials(
			@Param("courseId") Long courseId,
			@Param("username") String username
	);
}