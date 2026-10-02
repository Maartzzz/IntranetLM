package com.lmoving.intranet.courses.courseAudience;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lmoving.intranet.user.Role;

public interface CourseAudienceRepository
	extends JpaRepository<CourseAudience, Long> {

	List<CourseAudience> findByCourseId(Long courseId);

	void deleteByCourseId(Long courseId);

	boolean existsByCourseIdAndRole(
		Long courseId,
		Role role
	);

	List<CourseAudience> findByRoleAndCourseActiveTrue(Role role);
}