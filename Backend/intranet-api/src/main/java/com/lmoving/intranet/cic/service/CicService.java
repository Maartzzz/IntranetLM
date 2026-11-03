package com.lmoving.intranet.cic.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.lmoving.intranet.cic.CicCategoria;
import com.lmoving.intranet.cic.CicDocumento;
import com.lmoving.intranet.cic.CicEstadoDocumento;
import com.lmoving.intranet.cic.dto.CicDocumentoResponse;
import com.lmoving.intranet.cic.repository.CicCategoriaRepository;
import com.lmoving.intranet.cic.repository.CicDocumentoRepository;
import com.lmoving.intranet.user.User;
import com.lmoving.intranet.user.UserRepository;

@Service
public class CicService {

    private static final Set<String> EXTENSIONES_PERMITIDAS = Set.of(
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx"
    );

    private final CicCategoriaRepository categoriaRepository;
    private final CicDocumentoRepository documentoRepository;
    private final UserRepository userRepository;
    private final CicFileStorageService fileStorageService;

    public CicService(
            CicCategoriaRepository categoriaRepository,
            CicDocumentoRepository documentoRepository,
            UserRepository userRepository,
            CicFileStorageService fileStorageService) {

        this.categoriaRepository = categoriaRepository;
        this.documentoRepository = documentoRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional(readOnly = true)
    public List<CicDocumentoResponse> listarPublicados() {
        return documentoRepository
                .findByEstadoOrderByPublicadoEnDesc(
                        CicEstadoDocumento.PUBLICADO)
                .stream()
                .map(CicDocumentoResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public CicDocumentoResponse obtenerPublicado(Long id) {
        CicDocumento documento = obtenerDocumento(id);

        if (documento.getEstado() != CicEstadoDocumento.PUBLICADO) {
            throw new IllegalArgumentException(
                    "El documento no está publicado");
        }

        return CicDocumentoResponse.fromEntity(documento);
    }

    @Transactional(readOnly = true)
    public List<CicDocumentoResponse> listarAdministracion() {
        return documentoRepository.findAllByOrderByCreadoEnDesc()
                .stream()
                .map(CicDocumentoResponse::fromEntity)
                .toList();
    }

    @Transactional
    public CicDocumentoResponse crearDocumento(
            String titulo,
            String descripcion,
            Long categoriaId,
            MultipartFile archivo,
            String username) {

        validarTitulo(titulo);
        validarArchivo(archivo);

        if (categoriaId == null) {
            throw new IllegalArgumentException(
                    "La categoría es obligatoria");
        }

        CicCategoria categoria = categoriaRepository
                .findById(categoriaId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "La categoría indicada no existe"));

        if (!categoria.isActiva()) {
            throw new IllegalArgumentException(
                    "La categoría está inactiva");
        }

        User usuario = userRepository.findById(username)
                .orElseThrow(() -> new IllegalArgumentException(
                        "El usuario no existe"));

        String nombreOriginal = archivo.getOriginalFilename()
                .replace("\\", "/");

        nombreOriginal = nombreOriginal.substring(
                nombreOriginal.lastIndexOf('/') + 1);

        String nombreFisico = fileStorageService.store(archivo);

        try {
            CicDocumento documento = new CicDocumento();

            documento.setTitulo(titulo.trim());
            documento.setDescripcion(
                    descripcion == null || descripcion.isBlank()
                            ? null
                            : descripcion.trim());
            documento.setCategoria(categoria);
            documento.setNombreArchivo(nombreOriginal);
            documento.setRutaArchivo(nombreFisico);
            documento.setTipoMime(determinarMime(nombreOriginal));
            documento.setTamanoBytes(archivo.getSize());
            documento.setEstado(CicEstadoDocumento.BORRADOR);
            documento.setCreadoPor(usuario);

            return CicDocumentoResponse.fromEntity(
                    documentoRepository.save(documento));

        } catch (RuntimeException e) {
            fileStorageService.delete(nombreFisico);
            throw e;
        }
    }

    @Transactional
    public CicDocumentoResponse cambiarEstado(
            Long id,
            CicEstadoDocumento nuevoEstado) {

        if (nuevoEstado == null) {
            throw new IllegalArgumentException(
                    "El estado es obligatorio");
        }

        CicDocumento documento = obtenerDocumento(id);

        if (nuevoEstado == CicEstadoDocumento.PUBLICADO
                && documento.getEstado()
                        != CicEstadoDocumento.PUBLICADO) {
            documento.setPublicadoEn(LocalDateTime.now());
        }

        if (nuevoEstado == CicEstadoDocumento.BORRADOR) {
            documento.setPublicadoEn(null);
        }

        documento.setEstado(nuevoEstado);

        return CicDocumentoResponse.fromEntity(
                documentoRepository.save(documento));
    }

    @Transactional(readOnly = true)
    public Resource descargarPublicado(Long id) {
        CicDocumento documento = obtenerDocumento(id);

        if (documento.getEstado() != CicEstadoDocumento.PUBLICADO) {
            throw new IllegalArgumentException(
                    "El documento no está publicado");
        }

        return fileStorageService.load(documento.getRutaArchivo());
    }

    private CicDocumento obtenerDocumento(Long id) {
        return documentoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró el documento solicitado"));
    }

    private void validarTitulo(String titulo) {
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException(
                    "El título es obligatorio");
        }

        if (titulo.trim().length() > 200) {
            throw new IllegalArgumentException(
                    "El título no puede superar los 200 caracteres");
        }
    }

    private void validarArchivo(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException(
                    "Debes adjuntar un archivo");
        }

        String nombre = archivo.getOriginalFilename();

        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException(
                    "El archivo no tiene un nombre válido");
        }

        String limpio = nombre.replace("\\", "/");
        limpio = limpio.substring(limpio.lastIndexOf('/') + 1);

        int punto = limpio.lastIndexOf('.');

        if (punto <= 0 || punto == limpio.length() - 1) {
            throw new IllegalArgumentException(
                    "El archivo debe tener una extensión válida");
        }

        String extension = limpio.substring(punto + 1)
                .toLowerCase(Locale.ROOT);

        if (!EXTENSIONES_PERMITIDAS.contains(extension)) {
            throw new IllegalArgumentException(
                    "Formato no permitido. Usa PDF, Word, Excel o PowerPoint");
        }
    }

    private String determinarMime(String nombreArchivo) {
        String nombre = nombreArchivo.toLowerCase(Locale.ROOT);

        if (nombre.endsWith(".pdf")) {
            return "application/pdf";
        }
        if (nombre.endsWith(".doc")) {
            return "application/msword";
        }
        if (nombre.endsWith(".docx")) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        }
        if (nombre.endsWith(".xls")) {
            return "application/vnd.ms-excel";
        }
        if (nombre.endsWith(".xlsx")) {
            return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        }
        if (nombre.endsWith(".ppt")) {
            return "application/vnd.ms-powerpoint";
        }
        if (nombre.endsWith(".pptx")) {
            return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
        }

        return "application/octet-stream";
    }
}
