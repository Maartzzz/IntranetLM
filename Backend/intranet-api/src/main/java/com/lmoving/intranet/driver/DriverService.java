package com.lmoving.intranet.driver;

import com.lmoving.intranet.driver.dto.CreateDriverRequest;
import com.lmoving.intranet.driver.dto.DriverResponse;
import com.lmoving.intranet.user.Role;
import com.lmoving.intranet.user.User;
import com.lmoving.intranet.user.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class DriverService {

	private final DriverRepository driverRepository;
	private final UserRepository userRepository;

	public DriverService(
			DriverRepository driverRepository,
			UserRepository userRepository) {

		this.driverRepository = driverRepository;
		this.userRepository = userRepository;
	}

	@Transactional
	public DriverResponse createDriver(
			CreateDriverRequest request) {

		User user = userRepository
				.findByUsername(request.username())
				.orElseThrow(() ->
						new ResponseStatusException(
								HttpStatus.NOT_FOUND,
								"Usuario no encontrado"
						)
				);

		if (user.getRole() != Role.CHOFER) {
			throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST,
					"El usuario no tiene el rol CHOFER"
			);
		}

		if (driverRepository
				.existsByUserUsername(request.username())) {

			throw new ResponseStatusException(
					HttpStatus.CONFLICT,
					"El usuario ya está registrado como conductor"
			);
		}

		if (driverRepository
				.existsByLicenseNumber(request.licenseNumber())) {

			throw new ResponseStatusException(
					HttpStatus.CONFLICT,
					"El número de licencia ya está registrado"
			);
		}

		if (request.licenseExpiryDate()
				.isBefore(request.licenseIssueDate())) {

			throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST,
					"La fecha de vencimiento no puede ser anterior a la fecha de emisión"
			);
		}

		Driver driver = new Driver();

		driver.setUser(user);
		driver.setLicenseNumber(
				request.licenseNumber()
		);
		driver.setLicenseCategory(
				request.licenseCategory()
		);
		driver.setLicenseIssueDate(
				request.licenseIssueDate()
		);
		driver.setLicenseExpiryDate(
				request.licenseExpiryDate()
		);
		driver.setActive(true);

		Driver savedDriver =
				driverRepository.save(driver);

		return toResponse(savedDriver);
	}

	@Transactional(readOnly = true)
	public List<DriverResponse> getAllDrivers() {

		return driverRepository
				.findAll()
				.stream()
				.map(this::toResponse)
				.toList();
	}

	private DriverResponse toResponse(
			Driver driver) {

		User user = driver.getUser();

		return new DriverResponse(
				user.getUsername(),
				user.getFirstName(),
				user.getLastName(),
				user.getDocumentType(),
				user.getDocumentNumber(),
				user.getPhone(),
				driver.getLicenseNumber(),
				driver.getLicenseCategory(),
				driver.getLicenseIssueDate(),
				driver.getLicenseExpiryDate(),
				driver.isActive()
		);
	}

	@Transactional(readOnly = true)
	public DriverResponse getDriver(String username) {

		Driver driver = driverRepository
				.findByUserUsername(username)
				.orElseThrow(() ->
						new ResponseStatusException(
								HttpStatus.NOT_FOUND,
								"Conductor no encontrado"
						)
				);

		return toResponse(driver);
	}

}