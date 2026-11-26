package com.lmoving.intranet.client.service;

import java.util.List;
import java.text.Normalizer;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.lmoving.intranet.client.Location;
import com.lmoving.intranet.client.dto.LocationRequest;
import com.lmoving.intranet.client.dto.LocationResponse;
import com.lmoving.intranet.client.dto.LocationSaveResponse;
import com.lmoving.intranet.client.repository.LocationRepository;

@Service
public class LocationService {
    private final LocationRepository repository;
    public LocationService(LocationRepository repository) { this.repository = repository; }
    @Transactional(readOnly = true) public List<LocationResponse> listActive() { return repository.findAllByActiveTrueOrderByNameAsc().stream().map(LocationResponse::from).toList(); }
    @Transactional(readOnly = true) public List<LocationResponse> listAll() { return repository.findAllByOrderByNameAsc().stream().map(LocationResponse::from).toList(); }
    @Transactional(readOnly = true) public LocationResponse get(Long id) { return LocationResponse.from(find(id)); }
    @Transactional public LocationSaveResponse create(LocationRequest request) {
        Location existing = repository.findAllByActiveTrueOrderByNameAsc().stream()
            .filter(location -> samePhysicalLocation(location, request))
            .findFirst().orElse(null);
        if (existing != null) return new LocationSaveResponse(LocationResponse.from(existing), true);
        Location location = new Location();
        apply(location, request);
        location.setActive(true);
        return new LocationSaveResponse(LocationResponse.from(repository.save(location)), false);
    }
    @Transactional public LocationResponse update(Long id, LocationRequest request) { Location l=find(id); apply(l,request); return LocationResponse.from(repository.save(l)); }
    @Transactional public LocationResponse changeStatus(Long id, boolean active) { Location l=find(id); l.setActive(active); return LocationResponse.from(repository.save(l)); }
    private Location find(Long id) { return repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró la ubicación solicitada")); }
    private boolean samePhysicalLocation(Location location, LocationRequest request) {
        return location.getLocationType() == request.locationType()
            && normalize(location.getName()).equals(normalize(request.name()))
            && normalize(location.getAddress()).equals(normalize(request.address()))
            && normalize(location.getDistrict()).equals(normalize(request.district()))
            && normalize(location.getProvince()).equals(normalize(request.province()))
            && normalize(location.getDepartment()).equals(normalize(request.department()));
    }
    private String normalize(String value) {
        if (value == null) return "";
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
            .replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT).trim();
        return normalized.replaceAll("\\s+", " ");
    }
    private void apply(Location l, LocationRequest r) { l.setName(r.name().trim()); l.setLocationType(r.locationType()); l.setAddress(r.address().trim()); l.setReference(clean(r.reference())); l.setDistrict(clean(r.district())); l.setProvince(clean(r.province())); l.setDepartment(clean(r.department())); l.setLatitude(r.latitude()); l.setLongitude(r.longitude()); }
    private String clean(String v) { return v == null || v.isBlank() ? null : v.trim(); }
}
