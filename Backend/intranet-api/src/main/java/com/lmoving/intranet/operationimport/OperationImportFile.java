package com.lmoving.intranet.operationimport;

import jakarta.persistence.*;

@Entity
@Table(name = "operation_import_files")
public class OperationImportFile {
    @Id
    @Column(name = "batch_id")
    private Long batchId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "batch_id")
    private OperationImportBatch batch;

    @Column(name = "original_file", nullable = false, columnDefinition = "BYTEA")
    private byte[] originalFile;

    public OperationImportFile() {}
    public OperationImportFile(OperationImportBatch batch, byte[] originalFile) {
        this.batch = batch;
        this.originalFile = originalFile;
    }
    public Long getBatchId() { return batchId; }
    public OperationImportBatch getBatch() { return batch; }
    public byte[] getOriginalFile() { return originalFile; }
}
