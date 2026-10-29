package com.lmoving.intranet.cic.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CicFileStorageService {

    private final Path uploadDirectory;

    public CicFileStorageService(
            @Value("${app.cic.upload-dir:uploads/cic}")
            String uploadDir) {

        this.uploadDirectory = Paths.get(uploadDir)
                .toAbsolutePath()
                .normalize();

        try {
            Files.createDirectories(this.uploadDirectory);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "No se pudo crear el directorio de archivos del CIC",
                    e);
        }
    }

    public String store(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "El archivo está vacío");
        }

        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException(
                    "El archivo no tiene nombre");
        }

        // Solo conservar la extensión; nunca usar el nombre original
        // como ruta física.
        String safeName = Paths.get(
                originalFilename.replace("\\", "/"))
                .getFileName()
                .toString();

        int lastDot = safeName.lastIndexOf('.');
        String extension = lastDot > 0
                ? safeName.substring(lastDot).toLowerCase()
                : "";

        String filename = UUID.randomUUID() + extension;
        Path destination = uploadDirectory.resolve(filename).normalize();

        if (!destination.startsWith(uploadDirectory)) {
            throw new IllegalArgumentException(
                    "Ruta de archivo inválida");
        }

        try {
            Files.copy(
                    file.getInputStream(),
                    destination,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "No se pudo guardar el archivo del CIC",
                    e);
        }

        return filename;
    }

    public Resource load(String filename) {

        if (filename == null || filename.isBlank()
                || filename.contains("/")
                || filename.contains("\\")
                || filename.equals(".")
                || filename.equals("..")) {
            throw new IllegalArgumentException(
                    "Nombre de archivo inválido");
        }

        Path file = uploadDirectory.resolve(filename).normalize();

        if (!file.startsWith(uploadDirectory)) {
            throw new IllegalArgumentException(
                    "Ruta de archivo inválida");
        }

        try {
            Resource resource = new UrlResource(file.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new IllegalArgumentException(
                        "Archivo no encontrado");
            }

            return resource;
        } catch (IOException e) {
            throw new IllegalStateException(
                    "No se pudo leer el archivo del CIC",
                    e);
        }
    }

    public void delete(String filename) {

        if (filename == null || filename.isBlank()
                || filename.contains("/")
                || filename.contains("\\")
                || filename.equals(".")
                || filename.equals("..")) {
            throw new IllegalArgumentException(
                    "Nombre de archivo inválido");
        }

        Path file = uploadDirectory.resolve(filename).normalize();

        if (!file.startsWith(uploadDirectory)) {
            throw new IllegalArgumentException(
                    "Ruta de archivo inválida");
        }

        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "No se pudo eliminar el archivo del CIC",
                    e);
        }
    }
}
