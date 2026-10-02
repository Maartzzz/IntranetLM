package com.lmoving.intranet.driver;

import com.lmoving.intranet.user.User;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "drivers")
public class Driver {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(
		name = "username",
		referencedColumnName = "username",
		nullable = false,
		unique = true
	)
	private User user;

	@Column(name = "license_number", nullable = false, unique = true)
	private String licenseNumber;

	@Column(name = "license_category", nullable = false)
	private String licenseCategory;

	@Column(name = "license_issue_date", nullable = false)
	private LocalDate licenseIssueDate;

	@Column(name = "license_expiry_date", nullable = false)
	private LocalDate licenseExpiryDate;

	@Column(nullable = false)
	private boolean active = true;

	public Driver() {
	}

	public Long getId() {
		return id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getLicenseNumber() {
		return licenseNumber;
	}

	public void setLicenseNumber(String licenseNumber) {
		this.licenseNumber = licenseNumber;
	}

	public String getLicenseCategory() {
		return licenseCategory;
	}

	public void setLicenseCategory(String licenseCategory) {
		this.licenseCategory = licenseCategory;
	}

	public LocalDate getLicenseIssueDate() {
		return licenseIssueDate;
	}

	public void setLicenseIssueDate(LocalDate licenseIssueDate) {
		this.licenseIssueDate = licenseIssueDate;
	}

	public LocalDate getLicenseExpiryDate() {
		return licenseExpiryDate;
	}

	public void setLicenseExpiryDate(LocalDate licenseExpiryDate) {
		this.licenseExpiryDate = licenseExpiryDate;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
}