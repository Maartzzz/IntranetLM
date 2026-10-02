package com.lmoving.intranet.courses.courseAudience.dto;

import com.lmoving.intranet.user.Role;

public record CourseAudienceResponse(
        Long id,
        Long courseId,
        Role role
) {
}