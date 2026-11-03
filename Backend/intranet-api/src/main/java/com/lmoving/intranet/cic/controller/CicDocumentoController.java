package com.lmoving.intranet.cic.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.lmoving.intranet.cic.CicEstadoDocumento;
import com.lmoving.intranet.cic.dto.CicDocumentoResponse;
import com.lmoving.intranet.cic.service.CicService;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/cic")
public class CicDocumentoController {

    private final CicService cicService;

    public CicDocumentoController(CicService cicService) {
        this.cicService = cicService;
    }

    // Documentos visibles para cualquier usuario autenticado

    @GetMapping("/documentos")
    public ResponseEntity<List<CicDocumentoResponse>> listarPublicados() {
        return ResponseEntity.ok(cicService.listarPublicados());
    }

    @GetMapping("/documentos/{id}")
    public ResponseEntity<CicDocumentoResponse> obtenerPublicado(
            @PathVariable Long id) {
        return ResponseEntity.ok(cicService.obtenerPublicado(id));
    }

    @GetMapping("/documentos/{id}/archivo")
    public ResponseEntity<Resource> descargarArchivo(
            @PathVariable Long id) {

        CicDocumentoResponse documento =
                cicService.obtenerPublicado(id);

        Resource archivo = cicService.descargarPublicado(id);

        String nombreCodificado = URLEncoder.encode(
                documento.getNombreArchivo(),
                StandardCharsets.UTF_8
        ).replace("+", "%20");

        MediaType mediaType = tipoMimeSeguro(documento.getTipoMime());

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + nombreCodificado
                )
                .body(archivo);
    }

    // Endpoints exclusivos para administradores

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/documentos")
    public ResponseEntity<List<CicDocumentoResponse>> listarAdministracion() {
        return ResponseEntity.ok(cicService.listarAdministracion());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(
            value = "/admin/documentos",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<CicDocumentoResponse> crearDocumento(
            @RequestParam("titulo") String titulo,
            @RequestParam(value = "descripcion", required = false)
                    String descripcion,
            @RequestParam("categoriaId") Long categoriaId,
            @RequestPart("archivo") MultipartFile archivo,
            Authentication authentication) {

        CicDocumentoResponse creado = cicService.crearDocumento(
                titulo,
                descripcion,
                categoriaId,
                archivo,
                authentication.getName()
        );

        return ResponseEntity.ok(creado);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/admin/documentos/{id}/estado")
    public ResponseEntity<CicDocumentoResponse> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody CambiarEstadoRequest request) {

        CicEstadoDocumento estado;

        try {
            estado = CicEstadoDocumento.valueOf(
                    request.estado().trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException(
                    "Estado inválido. Usa BORRADOR, PUBLICADO o ARCHIVADO");
        }

        return ResponseEntity.ok(
                cicService.cambiarEstado(id, estado));
    }

    public record CambiarEstadoRequest(
            @NotNull String estado
    ) {
    }

    private MediaType tipoMimeSeguro(String tipoMime) {
        try {
            return MediaType.parseMediaType(tipoMime);
        } catch (Exception e) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
