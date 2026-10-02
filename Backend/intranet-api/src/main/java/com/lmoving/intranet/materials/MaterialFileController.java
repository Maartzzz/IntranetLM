package com.lmoving.intranet.materials;

import java.nio.file.Paths;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/materials")
public class MaterialFileController {

	private final FileStorageService fileStorageService;
	private final CourseMaterialService materialService;

	public MaterialFileController(
			FileStorageService fileStorageService,
			CourseMaterialService materialService
	) {
		this.fileStorageService = fileStorageService;
		this.materialService = materialService;
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@PostMapping(
		value = "/upload",
		consumes = "multipart/form-data"
	)
	@ResponseStatus(HttpStatus.CREATED)
	public Map<String, String> upload(
			@RequestParam("file")
			MultipartFile file
	) {

		String filename =
				fileStorageService.store(file);

		return Map.of(
				"filename", filename,
				"url", "/uploads/materials/" + filename
		);
	}

	@GetMapping("/{materialId}/content")
	public ResponseEntity<Resource> getContent(
			@PathVariable Long materialId
	) {

		CourseMaterial material =
				materialService.getEntityById(
						materialId
				);

		String url = material.getUrl();

		if (url == null ||
				!url.startsWith("/uploads/materials/")) {

			return ResponseEntity
					.badRequest()
					.build();
		}

		String filename =
				Paths.get(url)
						.getFileName()
						.toString();

		Resource resource =
				fileStorageService.load(filename);

		return ResponseEntity.ok()
				.contentType(
						MediaTypeFactory
								.getMediaType(resource)
								.orElse(
										MediaType.APPLICATION_OCTET_STREAM
								)
				)
				.body(resource);
	}
}