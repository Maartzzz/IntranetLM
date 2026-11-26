package com.lmoving.intranet.operationimport;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.lmoving.intranet.operationimport.dto.ImportDtos.*;

@RestController
@RequestMapping("/api/operation-imports")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR', 'OPERACIONES')")
public class OperationImportController {
    private final OperationImportService service;
    public OperationImportController(OperationImportService service) { this.service = service; }

    @PostMapping(value = "/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public PreviewResponse preview(@RequestPart("file") MultipartFile file) {
        return service.crearPrevisualizacion(file);
    }

    @GetMapping("/{id}/inspect")
    public PreviewResponse inspect(@PathVariable Long id,
            @RequestParam int sheetIndex, @RequestParam(required = false) Integer headerRow) {
        return service.inspeccionar(id, sheetIndex, headerRow);
    }

    @PostMapping("/{id}/analyze")
    public AnalyzeResponse analyze(@PathVariable Long id, @RequestBody AnalyzeRequest request) {
        return service.analizar(id, request);
    }

    @PostMapping("/{id}/execute")
    public ExecuteResponse execute(@PathVariable Long id, @RequestBody ExecuteRequest request) {
        return service.ejecutar(id, request);
    }

    @GetMapping
    public List<HistoryResponse> history() { return service.historial(); }

    @GetMapping("/{id}/file")
    public ResponseEntity<byte[]> originalFile(@PathVariable Long id) {
        OperationImportService.DownloadFile file = service.descargarOriginal(id);
        MediaType type;
        try { type = file.contentType() == null ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(file.contentType()); }
        catch (Exception ex) { type = MediaType.APPLICATION_OCTET_STREAM; }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(type);
        headers.setContentDisposition(ContentDisposition.attachment().filename(file.fileName(), StandardCharsets.UTF_8).build());
        headers.setContentLength(file.content().length);
        return new ResponseEntity<>(file.content(), headers, HttpStatus.OK);
    }

    @GetMapping("/profiles")
    public List<ProfileResponse> profiles() { return service.listarPerfiles(); }

    @PostMapping("/profiles")
    @ResponseStatus(HttpStatus.CREATED)
    public ProfileResponse saveProfile(@RequestBody ProfileRequest request) { return service.guardarPerfil(request); }

    @DeleteMapping("/profiles/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProfile(@PathVariable Long id) { service.eliminarPerfil(id); }
}
