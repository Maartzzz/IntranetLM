package com.lmoving.intranet.certificate;

import com.lmoving.intranet.evaluation.EvaluationAttempt;
import com.lmoving.intranet.evaluation.EvaluationAttemptRepository;
import com.lmoving.intranet.user.User;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CertificateService {

	private static final String COMPANY_NAME = "LOGISTICS MOVING";

	private static final DateTimeFormatter DATE_FORMATTER =
			DateTimeFormatter.ofPattern("dd/MM/yyyy");

	private final EvaluationAttemptRepository evaluationAttemptRepository;

	public CertificateService(
			EvaluationAttemptRepository evaluationAttemptRepository
	) {
		this.evaluationAttemptRepository = evaluationAttemptRepository;
	}

	public byte[] generateCertificate(
			Long attemptId,
			String username
	) {

		EvaluationAttempt attempt = evaluationAttemptRepository
				.findById(attemptId)
				.orElseThrow(() ->
						new IllegalArgumentException(
								"No existe el intento de evaluación con id: "
										+ attemptId
						)
				);

		if (!attempt.getUser().getUsername().equals(username)) {
			throw new IllegalStateException(
					"El intento de evaluación no pertenece al usuario autenticado"
			);
		}

		if (!Boolean.TRUE.equals(attempt.getPassed())) {
			throw new IllegalStateException(
					"No se puede generar un certificado para una evaluación no aprobada"
			);
		}

		if (attempt.getCompletedAt() == null) {
			throw new IllegalStateException(
					"La evaluación aún no ha sido finalizada"
			);
		}

		try {
			return buildPdf(attempt);
		} catch (DocumentException e) {
			throw new IllegalStateException(
					"No se pudo generar el certificado PDF",
					e
			);
		}
	}

	private byte[] buildPdf(
			EvaluationAttempt attempt
	) throws DocumentException {

		User user = attempt.getUser();

		ByteArrayOutputStream outputStream =
				new ByteArrayOutputStream();

		Document document = new Document(
				PageSize.A4.rotate(),
				48,
				48,
				48,
				48
		);

		PdfWriter.getInstance(
				document,
				outputStream
		);

		document.open();

		Font companyFont = new Font(
				Font.HELVETICA,
				14,
				Font.BOLD,
				new Color(37, 99, 235)
		);

		Font titleFont = new Font(
				Font.HELVETICA,
				28,
				Font.BOLD,
				new Color(15, 23, 42)
		);

		Font subtitleFont = new Font(
				Font.HELVETICA,
				13,
				Font.NORMAL,
				new Color(71, 85, 105)
		);

		Font nameFont = new Font(
				Font.HELVETICA,
				24,
				Font.BOLD,
				new Color(15, 23, 42)
		);

		Font courseFont = new Font(
				Font.HELVETICA,
				19,
				Font.BOLD,
				new Color(37, 99, 235)
		);

		Font detailLabelFont = new Font(
				Font.HELVETICA,
				10,
				Font.BOLD,
				new Color(100, 116, 139)
		);

		Font detailValueFont = new Font(
				Font.HELVETICA,
				11,
				Font.NORMAL,
				new Color(15, 23, 42)
		);

		// Empresa
		Paragraph company =
				new Paragraph(
						COMPANY_NAME,
						companyFont
				);

		company.setAlignment(
				Element.ALIGN_CENTER
		);

		company.setSpacingAfter(18);

		document.add(company);

		// Título
		Paragraph title =
				new Paragraph(
						"CERTIFICADO DE APROBACIÓN",
						titleFont
				);

		title.setAlignment(
				Element.ALIGN_CENTER
		);

		title.setSpacingAfter(14);

		document.add(title);

		// Introducción
		Paragraph introduction =
				new Paragraph(
						"Se certifica que",
						subtitleFont
				);

		introduction.setAlignment(
				Element.ALIGN_CENTER
		);

		introduction.setSpacingAfter(12);

		document.add(introduction);

		// Nombre
		Paragraph studentName =
				new Paragraph(
						user.getFirstName()
								+ " "
								+ user.getLastName(),
						nameFont
				);

		studentName.setAlignment(
				Element.ALIGN_CENTER
		);

		studentName.setSpacingAfter(8);

		document.add(studentName);

		// Documento
		Paragraph documentNumber =
				new Paragraph(
						user.getDocumentType().name()
								+ ": "
								+ user.getDocumentNumber(),
						subtitleFont
				);

		documentNumber.setAlignment(
				Element.ALIGN_CENTER
		);

		documentNumber.setSpacingAfter(22);

		document.add(documentNumber);

		// Curso
		Paragraph completedCourse =
				new Paragraph(
						"ha completado satisfactoriamente el curso",
						subtitleFont
				);

		completedCourse.setAlignment(
				Element.ALIGN_CENTER
		);

		completedCourse.setSpacingAfter(10);

		document.add(completedCourse);

		Paragraph course =
				new Paragraph(
						attempt.getCourse().getTitle(),
						courseFont
				);

		course.setAlignment(
				Element.ALIGN_CENTER
		);

		course.setSpacingAfter(28);

		document.add(course);

		// Datos
		PdfPTable details =
				new PdfPTable(3);

		details.setWidthPercentage(82);

		details.setWidths(
				new float[]{1f, 1f, 1f}
		);

		addDetail(
				details,
				"CALIFICACIÓN",
				attempt.getScore().toPlainString()
		);

		addDetail(
				details,
				"PUNTAJE MÍNIMO",
				attempt.getCourse()
						.getPassingScore()
						.toString()
		);

		addDetail(
				details,
				"FECHA DE APROBACIÓN",
				attempt.getCompletedAt()
						.format(DATE_FORMATTER)
		);

		document.add(details);

		// Código
		Paragraph certificateCode =
				new Paragraph(
						"Código de certificado: CERT-"
								+ String.format(
										"%08d",
										attempt.getId()
								),
						detailValueFont
				);

		certificateCode.setAlignment(
				Element.ALIGN_CENTER
		);

		certificateCode.setSpacingBefore(28);

		document.add(certificateCode);

		// Footer
		Paragraph footer =
				new Paragraph(
						COMPANY_NAME,
						detailLabelFont
				);

		footer.setAlignment(
				Element.ALIGN_CENTER
		);

		footer.setSpacingBefore(18);

		document.add(footer);

		document.close();

		return outputStream.toByteArray();
	}

	private void addDetail(
			PdfPTable table,
			String label,
			String value
	) {

		PdfPCell cell =
				new PdfPCell();

		cell.setBorder(
				PdfPCell.NO_BORDER
		);

		cell.setHorizontalAlignment(
				Element.ALIGN_CENTER
		);

		cell.setPadding(8);

		Paragraph labelParagraph =
				new Paragraph(
						label,
						new Font(
								Font.HELVETICA,
								10,
								Font.BOLD,
								new Color(
										100,
										116,
										139
								)
						)
				);

		labelParagraph.setAlignment(
				Element.ALIGN_CENTER
		);

		Paragraph valueParagraph =
				new Paragraph(
						value,
						new Font(
								Font.HELVETICA,
								12,
								Font.BOLD,
								new Color(
										15,
										23,
										42
								)
						)
				);

		valueParagraph.setAlignment(
				Element.ALIGN_CENTER
		);

		cell.addElement(
				labelParagraph
		);

		cell.addElement(
				valueParagraph
		);

		table.addCell(cell);
	}

	public List<CertificateResponse> getActiveCertificates() {

		return evaluationAttemptRepository
				.findByPassedTrueAndCompletedAtIsNotNullOrderByCompletedAtDesc()
				.stream()
				.map(attempt -> {

					User user = attempt.getUser();

					return new CertificateResponse(
							attempt.getId(),
							user.getUsername(),
							user.getFirstName() + " " + user.getLastName(),
							user.getDocumentType().name(),
							user.getDocumentNumber(),
							attempt.getCourse().getId(),
							attempt.getCourse().getTitle(),
							attempt.getScore(),
							attempt.getCourse().getPassingScore(),
							attempt.getCompletedAt(),
							"CERT-" + String.format("%08d", attempt.getId())
					);
				})
				.toList();
	}

	public byte[] generateCertificateForAdmin(Long attemptId) {

		EvaluationAttempt attempt =
				evaluationAttemptRepository
						.findById(attemptId)
						.orElseThrow(() ->
								new IllegalArgumentException(
										"Intento de evaluación no encontrado"
								)
						);

		if (!Boolean.TRUE.equals(attempt.getPassed())) {
			throw new IllegalStateException(
					"El intento de evaluación no fue aprobado"
			);
		}

		if (attempt.getCompletedAt() == null) {
			throw new IllegalStateException(
					"El intento de evaluación no está completado"
			);
		}

		return buildPdf(attempt);
	}
	
}