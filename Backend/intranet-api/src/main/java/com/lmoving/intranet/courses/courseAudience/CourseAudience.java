package com.lmoving.intranet.courses.courseAudience;

import com.lmoving.intranet.courses.Courses;
import com.lmoving.intranet.user.Role;

import jakarta.persistence.*;

@Entity
@Table(
    name = "course_audiences",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_course_audience",
            columnNames = {"course_id", "role"}
        )
    }
)
public class CourseAudience {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "course_id",
        nullable = false
    )
    private Courses course;

    @Enumerated(EnumType.STRING)
    @Column(
        name = "role",
        nullable = false,
        length = 30
    )
    private Role role;

    public CourseAudience() {
    }

    public CourseAudience(
            Courses course,
            Role role
    ) {
        this.course = course;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public Courses getCourse() {
        return course;
    }

    public void setCourse(Courses course) {
        this.course = course;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}