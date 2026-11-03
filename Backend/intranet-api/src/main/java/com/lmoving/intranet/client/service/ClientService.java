package com.lmoving.intranet.client.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lmoving.intranet.client.Client;
import com.lmoving.intranet.client.dto.ClientRequest;
import com.lmoving.intranet.client.dto.ClientResponse;
import com.lmoving.intranet.client.repository.ClientRepository;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Transactional(readOnly = true)
    public List<ClientResponse> listActive() {
        return clientRepository.findAllByActiveTrueOrderByBusinessNameAsc()
                .stream().map(ClientResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ClientResponse> listAll() {
        return clientRepository.findAllByOrderByBusinessNameAsc()
                .stream().map(ClientResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ClientResponse getById(Long id) {
        return ClientResponse.from(findClient(id));
    }

    @Transactional
    public ClientResponse create(ClientRequest request) {
        String number = normalizeDocumentNumber(request.documentNumber());
        validateDocument(request.documentType().name(), number);
        ensureDocumentAvailable(request, number, null);

        Client client = new Client();
        apply(client, request, number);
        client.setActive(true);
        return ClientResponse.from(clientRepository.save(client));
    }

    @Transactional
    public ClientResponse update(Long id, ClientRequest request) {
        Client client = findClient(id);
        String number = normalizeDocumentNumber(request.documentNumber());
        validateDocument(request.documentType().name(), number);
        ensureDocumentAvailable(request, number, id);
        apply(client, request, number);
        return ClientResponse.from(clientRepository.save(client));
    }

    @Transactional
    public ClientResponse changeStatus(Long id, boolean active) {
        Client client = findClient(id);
        client.setActive(active);
        return ClientResponse.from(clientRepository.save(client));
    }

    private Client findClient(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el cliente solicitado"));
    }

    private void apply(Client client, ClientRequest request, String number) {
        client.setDocumentType(request.documentType());
        client.setDocumentNumber(number);
        client.setBusinessName(request.businessName().trim());
        client.setAddress(clean(request.address()));
    }

    private void ensureDocumentAvailable(ClientRequest request, String number, Long currentId) {
        clientRepository.findByDocumentTypeAndDocumentNumber(request.documentType(), number)
            .filter(existing -> !existing.getId().equals(currentId))
            .ifPresent(existing -> { throw new IllegalArgumentException("Ya existe un cliente con ese tipo y número de documento"); });
    }

    private String normalizeDocumentNumber(String value) {
        return value.trim().replaceAll("\\s+", "");
    }

    private void validateDocument(String type, String number) {
        boolean valid = switch (type) {
            case "RUC" -> number.matches("\\d{11}");
            case "DNI" -> number.matches("\\d{8}");
            case "CE" -> number.matches("[A-Za-z0-9]{5,20}");
            default -> false;
        };
        if (!valid) {
            throw new IllegalArgumentException(switch (type) {
                case "RUC" -> "El RUC debe contener exactamente 11 dígitos";
                case "DNI" -> "El DNI debe contener exactamente 8 dígitos";
                case "CE" -> "El CE debe contener entre 5 y 20 caracteres alfanuméricos";
                default -> "Tipo de documento no válido";
            });
        }
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
