package com.lmoving.intranet.cargotype.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lmoving.intranet.cargotype.CargoType;
import com.lmoving.intranet.cargotype.repository.CargoTypeRepository;

@Service
public class CargoTypeService {

    private final CargoTypeRepository cargoTypeRepository;

    public CargoTypeService(CargoTypeRepository cargoTypeRepository) {
        this.cargoTypeRepository = cargoTypeRepository;
    }

    @Transactional(readOnly = true)
    public List<CargoType> listActive() {
        return cargoTypeRepository.findAllByOrderByNameAsc().stream()
                .filter(CargoType::isActive)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CargoType> listAll() {
        return cargoTypeRepository.findAllByOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public CargoType getById(Long id) {
        return findById(id);
    }

    @Transactional
    public CargoType create(String name, boolean container) {
        String cleanName = validateName(name);
        if (cargoTypeRepository.existsByNameIgnoreCase(cleanName)) {
            throw new IllegalArgumentException("Ya existe un tipo de carga con ese nombre");
        }

        CargoType cargoType = new CargoType();
        cargoType.setName(cleanName);
        cargoType.setContainer(container);
        cargoType.setActive(true);
        return cargoTypeRepository.save(cargoType);
    }

    @Transactional
    public CargoType update(Long id, String name, boolean container) {
        String cleanName = validateName(name);
        CargoType cargoType = findById(id);

        boolean duplicate = cargoTypeRepository.findAllByOrderByNameAsc().stream()
                .anyMatch(other -> !other.getId().equals(id)
                        && other.getName().equalsIgnoreCase(cleanName));
        if (duplicate) {
            throw new IllegalArgumentException("Ya existe otro tipo de carga con ese nombre");
        }

        cargoType.setName(cleanName);
        cargoType.setContainer(container);
        return cargoTypeRepository.save(cargoType);
    }

    @Transactional
    public CargoType changeStatus(Long id, boolean active) {
        CargoType cargoType = findById(id);
        cargoType.setActive(active);
        return cargoTypeRepository.save(cargoType);
    }

    private CargoType findById(Long id) {
        return cargoTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el tipo de carga solicitado"));
    }

    private String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre del tipo de carga es obligatorio");
        }
        String cleanName = name.trim();
        if (cleanName.length() > 100) {
            throw new IllegalArgumentException("El nombre no puede superar los 100 caracteres");
        }
        return cleanName;
    }
}
