package com.lmoving.intranet.provider.dto;

import java.time.LocalDateTime;
import com.lmoving.intranet.provider.Provider;
import com.lmoving.intranet.provider.ProviderDocumentType;

public record ProviderResponse(
    Long id, ProviderDocumentType documentType, String documentNumber, String businessName,
    String contactName, String phone, String email, String address, boolean active,
    LocalDateTime createdAt, LocalDateTime updatedAt
) {
    public static ProviderResponse from(Provider p) {
        return new ProviderResponse(p.getId(), p.getDocumentType(), p.getDocumentNumber(), p.getBusinessName(),
            p.getContactName(), p.getPhone(), p.getEmail(), p.getAddress(), p.isActive(), p.getCreatedAt(), p.getUpdatedAt());
    }
}
