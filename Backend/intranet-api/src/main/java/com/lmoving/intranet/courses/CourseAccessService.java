package com.lmoving.intranet.courses;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.lmoving.intranet.courses.courseAudience.CourseAudienceService;
import com.lmoving.intranet.user.User;
import com.lmoving.intranet.user.UserRepository;

/**
 * Centralizes the distinction between catalog access and personal learning access.
 * Users with catalog permission can access any course; other users can access
 * only active courses assigned to their current role.
 */
@Service
public class CourseAccessService {

    private final UserRepository userRepository;
    private final CourseAudienceService audienceService;

    public CourseAccessService(
            UserRepository userRepository,
            CourseAudienceService audienceService) {
        this.userRepository = userRepository;
        this.audienceService = audienceService;
    }

    public void ensureCanAccess(Long courseId, Authentication authentication) {
        boolean canViewCatalog = authentication.getAuthorities().stream()
                .anyMatch(authority -> "TRAINING.COURSES.VIEW".equals(authority.getAuthority()));

        if (canViewCatalog) {
            return;
        }

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Usuario no encontrado"));

        boolean assignedToUserRole = audienceService.getCoursesForRole(user.getRole()).stream()
                .anyMatch(course -> course.id().equals(courseId));

        if (!assignedToUserRole) {
            // 404 avoids disclosing whether an unassigned course exists.
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Curso no encontrado");
        }
    }
}
