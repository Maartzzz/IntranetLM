package com.lmoving.intranet.provider.service;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.lmoving.intranet.provider.Provider;
import com.lmoving.intranet.provider.ProviderDocumentType;
import com.lmoving.intranet.provider.dto.ProviderRequest;
import com.lmoving.intranet.provider.dto.ProviderResponse;
import com.lmoving.intranet.provider.repository.ProviderRepository;

@Service
public class ProviderService {
    private final ProviderRepository repository;

    public ProviderService(ProviderRepository repository) { this.repository = repository; }

    @Transactional(readOnly = true)
    public List<ProviderResponse> listActive() {
        return repository.findAllByActiveTrueOrderByBusinessNameAsc().stream().map(ProviderResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ProviderResponse> listAll() {
        return repository.findAllByOrderByBusinessNameAsc().stream().map(ProviderResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ProviderResponse get(Long id) { return ProviderResponse.from(find(id)); }

    @Transactional
    public ProviderResponse create(ProviderRequest request) {
        String number = normalizeDocument(request.documentNumber());
        validateDocument(request.documentType(), number);
        if (repository.existsByDocumentTypeAndDocumentNumber(request.documentType(), number)) {
            throw conflict("Ya existe un proveedor con ese tipo y número de documento");
        }
        Provider provider = new Provider();
        apply(provider, request, number);
        provider.setActive(true);
        return ProviderResponse.from(repository.save(provider));
    }

    @Transactional
    public ProviderResponse update(Long id, ProviderRequest request) {
        Provider provider = find(id);
        String number = normalizeDocument(request.documentNumber());
        validateDocument(request.documentType(), number);
        if (repository.existsByDocumentTypeAndDocumentNumberAndIdNot(request.documentType(), number, id)) {
            throw conflict("Ya existe otro proveedor con ese tipo y número de documento");
        }
        apply(provider, request, number);
        return ProviderResponse.from(repository.save(provider));
    }

    @Transactional
    public ProviderResponse changeStatus(Long id, boolean active) {
        Provider provider = find(id);
        provider.setActive(active);
        return ProviderResponse.from(repository.save(provider));
    }

    private void apply(Provider provider, ProviderRequest request, String number) {
        provider.setDocumentType(request.documentType());
        provider.setDocumentNumber(number);
        provider.setBusinessName(request.businessName().trim());
        provider.setContactName(clean(request.contactName()));
        provider.setPhone(clean(request.phone()));
        provider.setEmail(clean(request.email()));
        provider.setAddress(clean(request.address()));
    }

    private void validateDocument(ProviderDocumentType type, String number) {
        if (number == null || !number.matches("\\d+")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El número de documento debe contener solo dígitos");
        }
        boolean valid = switch (type) {
            case DNI -> number.matches("\\d{8}");
            case RUC10 -> number.matches("10\\d{9}");
            case RUC20 -> number.matches("20\\d{9}");
        };
        if (!valid) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                switch (type) {
                    case DNI -> "El DNI debe contener exactamente 8 dígitos";
                    case RUC10 -> "El RUC 10 debe contener 11 dígitos y comenzar con 10";
                    case RUC20 -> "El RUC 20 debe contener 11 dígitos y comenzar con 20";
                });
        }
    }

    private String normalizeDocument(String value) { return value == null ? null : value.trim(); }
    private String clean(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private Provider find(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el proveedor solicitado"));
    }
    private ResponseStatusException conflict(String message) { return new ResponseStatusException(HttpStatus.CONFLICT, message); }
}
