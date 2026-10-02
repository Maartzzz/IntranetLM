package com.lmoving.intranet.certificate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CertificateResponse {

    private Long attemptId;
    private String username;
    private String fullName;
    private String documentType;
    private String documentNumber;
    private Long courseId;
    private String courseTitle;
    private BigDecimal score;
    private Integer passingScore;
    private LocalDateTime completedAt;
    private String certificateCode;

    public CertificateResponse(
            Long attemptId,
            String username,
            String fullName,
            String documentType,
            String documentNumber,
            Long courseId,
            String courseTitle,
            BigDecimal score,
            Integer passingScore,
            LocalDateTime completedAt,
            String certificateCode
    ) {
        this.attemptId = attemptId;
        this.username = username;
        this.fullName = fullName;
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.courseId = courseId;
        this.courseTitle = courseTitle;
        this.score = score;
        this.passingScore = passingScore;
        this.completedAt = completedAt;
        this.certificateCode = certificateCode;
    }

    public Long getAttemptId() {
        return attemptId;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getDocumentType() {
        return documentType;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public Long getCourseId() {
        return courseId;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public BigDecimal getScore() {
        return score;
    }

    public Integer getPassingScore() {
        return passingScore;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public String getCertificateCode() {
        return certificateCode;
    }
}