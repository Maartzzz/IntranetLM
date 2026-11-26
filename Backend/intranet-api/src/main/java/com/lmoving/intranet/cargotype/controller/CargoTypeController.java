package com.lmoving.intranet.cargotype.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lmoving.intranet.cargotype.CargoType;
import com.lmoving.intranet.cargotype.service.CargoTypeService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/cargo-types")
public class CargoTypeController {

    private final CargoTypeService cargoTypeService;

    public CargoTypeController(CargoTypeService cargoTypeService) {
        this.cargoTypeService = cargoTypeService;
    }

    @PreAuthorize("hasAuthority('CONFIGURATION.CARGO_TYPES.VIEW')")
    @GetMapping
    public ResponseEntity<List<CargoType>> listActive() {
        return ResponseEntity.ok(cargoTypeService.listActive());
    }

    @PreAuthorize("hasAuthority('CONFIGURATION.CARGO_TYPES.VIEW')")
    @GetMapping("/{id}")
    public ResponseEntity<CargoType> getById(@PathVariable Long id) {
        return ResponseEntity.ok(cargoTypeService.getById(id));
    }

    @PreAuthorize("hasAuthority('CONFIGURATION.CARGO_TYPES.VIEW')")
    @GetMapping("/admin")
    public ResponseEntity<List<CargoType>> listAll() {
        return ResponseEntity.ok(cargoTypeService.listAll());
    }

    @PreAuthorize("hasAuthority('CONFIGURATION.CARGO_TYPES.CREATE')")
    @PostMapping
    public ResponseEntity<CargoType> create(@Valid @RequestBody CargoTypeRequest request) {
        CargoType created = cargoTypeService.create(request.name(), request.isContainer());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PreAuthorize("hasAuthority('CONFIGURATION.CARGO_TYPES.EDIT')")
    @PutMapping("/{id}")
    public ResponseEntity<CargoType> update(
            @PathVariable Long id,
            @Valid @RequestBody CargoTypeRequest request) {
        return ResponseEntity.ok(cargoTypeService.update(id, request.name(), request.isContainer()));
    }

    @PreAuthorize("hasAuthority('CONFIGURATION.CARGO_TYPES.DELETE')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<CargoType> changeStatus(
            @PathVariable Long id,
            @Valid @RequestBody CargoTypeStatusRequest request) {
        return ResponseEntity.ok(cargoTypeService.changeStatus(id, request.active()));
    }

    public record CargoTypeRequest(
            @NotBlank(message = "El nombre es obligatorio")
            @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
            String name,
            @NotNull(message = "Debe indicar si el tipo de carga es contenedor")
            Boolean isContainer) {
    }

    public record CargoTypeStatusRequest(
            @NotNull(message = "Debe indicar el estado")
            Boolean active) {
    }
}
