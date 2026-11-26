package com.lmoving.intranet.operationimport;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "operation_import_batches")
public class OperationImportBatch {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_file_name", nullable = false, length = 255)
    private String originalFileName;

    @Column(name = "content_type", length = 150)
    private String contentType;

    @Column(nullable = false, length = 30)
    private String status = "PREVIEW";

    @Column(name = "total_rows", nullable = false)
    private Integer totalRows = 0;

    @Column(name = "created_rows", nullable = false)
    private Integer createdRows = 0;

    @Column(name = "skipped_rows", nullable = false)
    private Integer skippedRows = 0;

    @Column(name = "mapping_json", columnDefinition = "TEXT")
    private String mappingJson;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist protected void onCreate() { createdAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String v) { originalFileName = v; }
    public String getContentType() { return contentType; }
    public void setContentType(String v) { contentType = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { status = v; }
    public Integer getTotalRows() { return totalRows; }
    public void setTotalRows(Integer v) { totalRows = v; }
    public Integer getCreatedRows() { return createdRows; }
    public void setCreatedRows(Integer v) { createdRows = v; }
    public Integer getSkippedRows() { return skippedRows; }
    public void setSkippedRows(Integer v) { skippedRows = v; }
    public String getMappingJson() { return mappingJson; }
    public void setMappingJson(String v) { mappingJson = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime v) { completedAt = v; }
}
