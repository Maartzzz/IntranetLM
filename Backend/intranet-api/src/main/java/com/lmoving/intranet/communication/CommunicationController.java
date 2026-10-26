
package com.lmoving.intranet.communication;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lmoving.intranet.communication.dto.CommunicationRequest;
import com.lmoving.intranet.communication.dto.CommunicationResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/communications")
public class CommunicationController {

    private final CommunicationService communicationService;

    public CommunicationController(
            CommunicationService communicationService) {
        this.communicationService = communicationService;
    }

    @PostMapping
    public ResponseEntity<CommunicationResponse> submit(
            @Valid @RequestBody CommunicationRequest request,
            Authentication authentication) {

        CommunicationResponse response =
            communicationService.submit(
                authentication.getName(),
                request
            );

        HttpStatus status =
            response.getEmailStatus() == CommunicationStatus.ENVIADO
                ? HttpStatus.CREATED
                : HttpStatus.ACCEPTED;

        return ResponseEntity.status(status).body(response);
    }
}
