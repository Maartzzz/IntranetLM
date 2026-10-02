package com.lmoving.intranet.materials;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseMaterialRepository
        extends JpaRepository<CourseMaterial, Long> {

    List<CourseMaterial> findByCourseIdOrderByCreatedAtAsc(
            Long courseId
    );
}