package com.lmoving.intranet.provider;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "providers", uniqueConstraints = {
    @UniqueConstraint(name = "uq_providers_document", columnNames = {"document_type", "document_number"})
})
public class Provider {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 10)
    private ProviderDocumentType documentType;

    @Column(name = "document_number", nullable = false, length = 11)
    private String documentNumber;

    @Column(name = "business_name", nullable = false, length = 200)
    private String businessName;

    @Column(name = "contact_name", length = 150)
    private String contactName;

    @Column(length = 30)
    private String phone;

    @Column(length = 150)
    private String email;

    @Column(length = 300)
    private String address;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Provider() {}

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public ProviderDocumentType getDocumentType() { return documentType; }
    public void setDocumentType(ProviderDocumentType value) { documentType = value; }
    public String getDocumentNumber() { return documentNumber; }
    public void setDocumentNumber(String value) { documentNumber = value; }
    public String getBusinessName() { return businessName; }
    public void setBusinessName(String value) { businessName = value; }
    public String getContactName() { return contactName; }
    public void setContactName(String value) { contactName = value; }
    public String getPhone() { return phone; }
    public void setPhone(String value) { phone = value; }
    public String getEmail() { return email; }
    public void setEmail(String value) { email = value; }
    public String getAddress() { return address; }
    public void setAddress(String value) { address = value; }
    public boolean isActive() { return active; }
    public void setActive(boolean value) { active = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
