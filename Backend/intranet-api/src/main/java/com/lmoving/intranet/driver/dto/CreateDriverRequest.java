package com.lmoving.intranet.driver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateDriverRequest(

	@NotBlank
	String username,

	@NotBlank
	@Size(max = 30)
	String licenseNumber,

	@NotBlank
	@Size(max = 20)
	String licenseCategory,

	@NotNull
	LocalDate licenseIssueDate,

	@NotNull
	LocalDate licenseExpiryDate
) {
}