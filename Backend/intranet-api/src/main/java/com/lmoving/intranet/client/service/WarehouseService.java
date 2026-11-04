package com.lmoving.intranet.client.service;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.lmoving.intranet.client.Location;
import com.lmoving.intranet.client.Warehouse;
import com.lmoving.intranet.client.dto.WarehouseRequest;
import com.lmoving.intranet.client.dto.WarehouseResponse;
import com.lmoving.intranet.client.repository.LocationRepository;
import com.lmoving.intranet.client.repository.WarehouseRepository;

@Service
public class WarehouseService {
    private final WarehouseRepository warehouses;
    private final LocationRepository locations;
    public WarehouseService(WarehouseRepository warehouses, LocationRepository locations) { this.warehouses = warehouses; this.locations = locations; }

    @Transactional(readOnly = true) public List<WarehouseResponse> listActive() { return warehouses.findAllByActiveTrueOrderByWarehouseNameAsc().stream().map(WarehouseResponse::from).toList(); }
    @Transactional(readOnly = true) public List<WarehouseResponse> listAll() { return warehouses.findAllByOrderByWarehouseNameAsc().stream().map(WarehouseResponse::from).toList(); }
    @Transactional(readOnly = true) public WarehouseResponse get(Long id) { return WarehouseResponse.from(find(id)); }
    @Transactional public WarehouseResponse create(WarehouseRequest r) {
        if (warehouses.existsByWarehouseCodeIgnoreCase(r.warehouseCode().trim())) throw conflict("Ya existe un almacén con ese código");
        Location location = findActiveLocation(r.locationId());
        if (warehouses.existsByLocationId(location.getId())) throw conflict("La ubicación seleccionada ya está registrada como almacén");
        Warehouse w = new Warehouse(); w.setLocation(location); apply(w, r); w.setActive(true);
        return WarehouseResponse.from(warehouses.save(w));
    }
    @Transactional public WarehouseResponse update(Long id, WarehouseRequest r) {
        Warehouse w = find(id);
        if (warehouses.existsByWarehouseCodeIgnoreCaseAndIdNot(r.warehouseCode().trim(), id)) throw conflict("Ya existe otro almacén con ese código");
        Location location = findActiveLocation(r.locationId());
        if (!w.getLocation().getId().equals(location.getId()) && warehouses.existsByLocationId(location.getId())) throw conflict("La ubicación seleccionada ya está registrada como almacén");
        w.setLocation(location); apply(w, r); return WarehouseResponse.from(warehouses.save(w));
    }
    @Transactional public WarehouseResponse changeStatus(Long id, boolean active) { Warehouse w = find(id); w.setActive(active); return WarehouseResponse.from(warehouses.save(w)); }
    private Warehouse find(Long id) { return warehouses.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el almacén solicitado")); }
    private Location findActiveLocation(Long id) { return locations.findById(id).filter(Location::isActive).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "La ubicación no existe o está inactiva")); }
    private void apply(Warehouse w, WarehouseRequest r) { w.setWarehouseCode(r.warehouseCode().trim()); w.setWarehouseName(r.warehouseName().trim()); w.setWarehouseType(r.warehouseType()); w.setOperatorName(r.operatorName().trim()); w.setObservations(clean(r.observations())); }
    private String clean(String v) { return v == null || v.isBlank() ? null : v.trim(); }
    private ResponseStatusException conflict(String message) { return new ResponseStatusException(HttpStatus.CONFLICT, message); }
}
