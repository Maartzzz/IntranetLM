package com.lmoving.intranet.certificate;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/certificates")
public class CertificateController {

	private final CertificateService certificateService;

	public CertificateController(
			CertificateService certificateService
	) {
		this.certificateService = certificateService;
	}

	@GetMapping("/attempts/{attemptId}")
	public ResponseEntity<byte[]> downloadCertificate(
			@PathVariable Long attemptId,
			Principal principal
	) {

		byte[] pdf =
				certificateService.generateCertificate(
						attemptId,
						principal.getName()
				);

		HttpHeaders headers =
				new HttpHeaders();

		headers.setContentType(
				MediaType.APPLICATION_PDF
		);

		headers.setContentDisposition(
				ContentDisposition.attachment()
						.filename(
								"certificado-"
										+ attemptId
										+ ".pdf"
						)
						.build()
		);

		return ResponseEntity.ok()
				.headers(headers)
				.body(pdf);
	}

	@GetMapping("/attempts/{attemptId}/download")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<byte[]> downloadCertificateAsAdmin(
			@PathVariable Long attemptId
	) {
	
		byte[] pdf =
				certificateService.generateCertificateForAdmin(attemptId);
	
		HttpHeaders headers = new HttpHeaders();
	
		headers.setContentType(
				MediaType.APPLICATION_PDF
		);
	
		headers.setContentDisposition(
				ContentDisposition.attachment()
						.filename(
								"certificado-"
										+ attemptId
										+ ".pdf"
						)
						.build()
		);
	
		return ResponseEntity.ok()
				.headers(headers)
				.body(pdf);
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	public ResponseEntity<List<CertificateResponse>> getActiveCertificates() {
		return ResponseEntity.ok(
				certificateService.getActiveCertificates()
		);
	}

}