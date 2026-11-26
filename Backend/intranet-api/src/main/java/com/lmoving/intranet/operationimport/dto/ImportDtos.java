package com.lmoving.intranet.operationimport.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public final class ImportDtos {
    private ImportDtos() {}
    public record PreviewResponse(Long importId, String fileName, List<String> sheets, int sheetIndex,
            int headerRow, List<String> headers, Map<String, Integer> suggestedMapping,
            List<List<String>> sampleRows) {}
    public record AnalyzeRequest(int sheetIndex, int headerRow, Map<String, Integer> mapping) {}
    public record RowResponse(int rowNumber, Map<String, String> values, boolean valid,
            boolean possibleDuplicate, List<String> issues) {}
    public record AnalyzeResponse(Long importId, int totalRows, int validRows, int duplicateRows,
            int invalidRows, List<RowResponse> rows) {}
    public record ExecuteRequest(int sheetIndex, int headerRow, Map<String, Integer> mapping,
            List<Integer> selectedRows) {}
    public record ExecuteResponse(Long importId, String status, int totalRows, int createdRows,
            int skippedRows, LocalDateTime completedAt) {}
    public record ProfileRequest(String name, Map<String, Integer> mapping) {}
    public record ProfileResponse(Long id, String name, Map<String, Integer> mapping, LocalDateTime updatedAt) {}
    public record HistoryResponse(Long id, String fileName, String status, int totalRows,
            int createdRows, int skippedRows, LocalDateTime createdAt, LocalDateTime completedAt) {}
}
