
package com.lmoving.intranet.communication;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.lmoving.intranet.communication.dto.CommunicationRequest;
import com.lmoving.intranet.communication.dto.CommunicationResponse;
import com.lmoving.intranet.user.User;
import com.lmoving.intranet.user.UserRepository;

@Service
public class CommunicationService {

    private final CommunicationRepository communicationRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;

    @Value("${app.communication.recipient:}")
    private String recipient;

    @Value("${spring.mail.username:}")
    private String senderAddress;

    public CommunicationService(
            CommunicationRepository communicationRepository,
            UserRepository userRepository,
            JavaMailSender mailSender) {

        this.communicationRepository = communicationRepository;
        this.userRepository = userRepository;
        this.mailSender = mailSender;
    }

    @Transactional
    public CommunicationResponse submit(
            String authenticatedUsername,
            CommunicationRequest request) {

        User user = userRepository.findByUsername(authenticatedUsername)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "No se encontró el usuario autenticado"
                )
            );

        Communication communication = new Communication();

        communication.setUser(user);
        communication.setSenderName(
            (user.getFirstName() + " " + user.getLastName()).trim()
        );
        communication.setType(request.getType());
        communication.setSubject(request.getSubject().trim());
        communication.setDescription(request.getDescription().trim());
        communication.setEmailStatus(CommunicationStatus.PENDIENTE);

        // Registrar primero la comunicación en PostgreSQL.
        communication = communicationRepository.saveAndFlush(communication);

        if (!StringUtils.hasText(recipient)) {
            communication.setEmailStatus(CommunicationStatus.ERROR);
            communication.setEmailError(
                "No se configuró COMMUNICATION_RECIPIENT"
            );

            communicationRepository.save(communication);

            return response(
                communication,
                "La comunicación quedó registrada, pero falta configurar el correo administrativo."
            );
        }

        try {
            SimpleMailMessage mail = new SimpleMailMessage();

            if (StringUtils.hasText(senderAddress)) {
                mail.setFrom(senderAddress);
            }

            mail.setTo(recipient);
            mail.setSubject(
                "[Intranet] " + label(request.getType())
                    + " — #" + communication.getId()
            );

            mail.setText(
                "Se recibió una nueva comunicación desde la intranet.\n\n"
                + "Número: " + communication.getId() + "\n"
                + "Tipo: " + label(request.getType()) + "\n"
                + "Trabajador: " + communication.getSenderName() + "\n"
                + "Usuario: " + user.getUsername() + "\n"
                + "Correo registrado: "
                + (user.getEmail() == null
                    ? "No registrado"
                    : user.getEmail()) + "\n"
                + "Fecha: " + communication.getCreatedAt() + "\n"
                + "Asunto: " + communication.getSubject() + "\n\n"
                + "Descripción:\n" + communication.getDescription()
            );

            mailSender.send(mail);

            communication.setEmailStatus(CommunicationStatus.ENVIADO);
            communication.setSentAt(LocalDateTime.now());
            communication.setEmailError(null);

            communicationRepository.save(communication);

            return response(
                communication,
                "Tu comunicación fue registrada y enviada correctamente."
            );

        } catch (MailException ex) {

            communication.setEmailStatus(CommunicationStatus.ERROR);
            communication.setEmailError(safeError(ex.getMessage()));

            communicationRepository.save(communication);

            return response(
                communication,
                "Tu comunicación quedó registrada, pero no se pudo enviar el correo."
            );
        }
    }

    private CommunicationResponse response(
            Communication communication,
            String message) {

        return new CommunicationResponse(
            communication.getId(),
            communication.getCreatedAt(),
            communication.getEmailStatus(),
            message
        );
    }

    private String label(CommunicationType type) {
        return switch (type) {
            case SUGERENCIA -> "Sugerencia";
            case QUEJA -> "Queja";
            case OTRO -> "Otro";
        };
    }

    private String safeError(String error) {
        if (!StringUtils.hasText(error)) {
            return "Error de envío de correo";
        }

        return error.length() > 500
            ? error.substring(0, 500)
            : error;
    }
}
