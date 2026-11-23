package com.lmoving.intranet.driver;

import com.lmoving.intranet.driver.dto.CreateDriverRequest;
import com.lmoving.intranet.driver.dto.DriverResponse;
import com.lmoving.intranet.driver.dto.UpdateDriverRequest;
import com.lmoving.intranet.user.Role;
import com.lmoving.intranet.user.User;
import com.lmoving.intranet.user.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.lmoving.intranet.audit.AuditLogService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class DriverService {

    @Autowired private AuditLogService auditLogService;

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

		Driver savedDriver = driverRepository.save(driver);
        DriverResponse response = toResponse(savedDriver);
        auditLogService.registrar("CONDUCTORES", "CONDUCTOR", savedDriver.getId(), request.username(), "CREATED",
                "Se registró como conductor al usuario " + request.username(), null, response.toString());
        return response;
	}

	@Transactional(readOnly = true)
	public List<DriverResponse> getAllDrivers() {

		return driverRepository
				.findAll()
				.stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional
	public void setActive(String username, boolean active) {
		Driver driver = driverRepository.findByUserUsername(username)
				.orElseThrow(() -> new ResponseStatusException(
						HttpStatus.NOT_FOUND,
						"Conductor no encontrado"
				));

        boolean previous = driver.isActive();
		driver.setActive(active);
		driverRepository.save(driver);
        auditLogService.registrar("CONDUCTORES", "CONDUCTOR", driver.getId(), username, active ? "ACTIVATED" : "DEACTIVATED",
                active ? "Se activó el conductor " + username : "Se desactivó el conductor " + username,
                Boolean.toString(previous), Boolean.toString(active));
	}


    @Transactional
    public DriverResponse updateDriver(
            String username,
            UpdateDriverRequest request) {

        Driver driver = driverRepository
                .findByUserUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Conductor no encontrado"
                ));

        if (request.licenseExpiryDate().isBefore(request.licenseIssueDate())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La fecha de vencimiento no puede ser anterior a la fecha de emisión"
            );
        }

        if (driverRepository.existsByLicenseNumberAndUserUsernameNot(
                request.licenseNumber(), username)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El número de licencia ya está registrado"
            );
        }

        DriverResponse oldValue = toResponse(driver);

        driver.setLicenseNumber(request.licenseNumber());
        driver.setLicenseCategory(request.licenseCategory());
        driver.setLicenseIssueDate(request.licenseIssueDate());
        driver.setLicenseExpiryDate(request.licenseExpiryDate());

        Driver savedDriver = driverRepository.save(driver);
        DriverResponse newValue = toResponse(savedDriver);

        auditLogService.registrar(
                "CONDUCTORES",
                "CONDUCTOR",
                savedDriver.getId(),
                username,
                "UPDATED",
                "Se actualizaron los datos del conductor " + username,
                oldValue.toString(),
                newValue.toString()
        );

        return newValue;
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