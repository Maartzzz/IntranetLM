package com.lmoving.intranet.materials;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

	private final Path uploadDirectory;

	public FileStorageService(
			@Value("${app.upload.dir:uploads/materials}")
			String uploadDir
	) {

		this.uploadDirectory =
				Paths.get(uploadDir)
						.toAbsolutePath()
						.normalize();

		try {

			Files.createDirectories(
					this.uploadDirectory
			);

		} catch (IOException e) {

			throw new RuntimeException(
					"No se pudo crear el directorio de archivos",
					e
			);
		}
	}

	public String store(
			MultipartFile file
	) {

		if (file == null ||
				file.isEmpty()) {

			throw new IllegalArgumentException(
					"El archivo está vacío"
			);
		}

		String originalFilename =
				file.getOriginalFilename();

		if (originalFilename == null ||
				originalFilename.isBlank()) {

			throw new IllegalArgumentException(
					"El archivo no tiene nombre"
			);
		}

		String extension =
				getExtension(originalFilename);

		String filename =
				UUID.randomUUID()
						+ extension;

		Path destination =
				uploadDirectory.resolve(filename)
						.normalize();

		if (!destination.startsWith(
				uploadDirectory
		)) {

			throw new IllegalArgumentException(
					"Nombre de archivo inválido"
			);
		}

		try {

			Files.copy(
					file.getInputStream(),
					destination,
					StandardCopyOption.REPLACE_EXISTING
			);

		} catch (IOException e) {

			throw new RuntimeException(
					"No se pudo guardar el archivo",
					e
			);
		}

		return filename;
	}

	private String getExtension(
			String filename
	) {

		int lastDot =
				filename.lastIndexOf('.');

		if (lastDot < 0) {
			return "";
		}

		return filename.substring(
				lastDot
		).toLowerCase();
	}

	public Resource load(
			String filename
	) {
	
		try {
	
			Path file =
					uploadDirectory
							.resolve(filename)
							.normalize();
	
			if (!file.startsWith(
					uploadDirectory
			)) {
	
				throw new IllegalArgumentException(
						"Ruta de archivo inválida"
				);
			}
	
			Resource resource =
					new UrlResource(
							file.toUri()
					);
	
			if (!resource.exists() ||
					!resource.isReadable()) {
	
				throw new RuntimeException(
						"Archivo no encontrado"
				);
			}
	
			return resource;
	
		} catch (IOException e) {
	
			throw new RuntimeException(
					"No se pudo leer el archivo",
					e
			);
		}
	}
}