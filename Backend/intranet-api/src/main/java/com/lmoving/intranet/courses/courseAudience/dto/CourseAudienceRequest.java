package com.lmoving.intranet.courses.courseAudience.dto;

import com.lmoving.intranet.user.Role;

import jakarta.validation.constraints.NotNull;

public record CourseAudienceRequest(

		@NotNull
		Role role

) {
}