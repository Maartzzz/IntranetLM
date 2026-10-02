package com.lmoving.intranet.driver.dto;

import com.lmoving.intranet.user.DocumentType;

import java.time.LocalDate;

public record DriverResponse(
	String username,
	String firstName,
	String lastName,
	DocumentType documentType,
	String documentNumber,
	String phone,
	String licenseNumber,
	String licenseCategory,
	LocalDate licenseIssueDate,
	LocalDate licenseExpiryDate,
	boolean active
) {
}