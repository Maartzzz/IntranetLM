package com.lmoving.intranet.client.service;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.lmoving.intranet.client.ClientSubclient;
import com.lmoving.intranet.client.ClientSubclientLocation;
import com.lmoving.intranet.client.Location;
import com.lmoving.intranet.client.dto.ClientSubclientLocationRequest;
import com.lmoving.intranet.client.dto.ClientSubclientLocationResponse;
import com.lmoving.intranet.client.repository.ClientSubclientLocationRepository;
import com.lmoving.intranet.client.repository.ClientSubclientRepository;
import com.lmoving.intranet.client.repository.LocationRepository;

@Service
public class ClientSubclientLocationService {
    private final ClientSubclientRepository relations;
    private final LocationRepository locations;
    private final ClientSubclientLocationRepository links;
    public ClientSubclientLocationService(ClientSubclientRepository relations, LocationRepository locations, ClientSubclientLocationRepository links) { this.relations=relations; this.locations=locations; this.links=links; }
    @Transactional(readOnly = true) public List<ClientSubclientLocationResponse> list(Long relationId) { findRelation(relationId); return links.findAllByClientSubclient_IdAndActiveTrueOrderByLocation_NameAsc(relationId).stream().map(ClientSubclientLocationResponse::from).toList(); }
    @Transactional public ClientSubclientLocationResponse add(Long relationId, ClientSubclientLocationRequest request) {
        ClientSubclient relation=findRelation(relationId);
        Location location=locations.findById(request.locationId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"No se encontró la ubicación"));
        if (!location.isActive()) throw new ResponseStatusException(HttpStatus.CONFLICT,"No se puede asociar una ubicación inactiva");
        ClientSubclientLocation link=links.findByClientSubclient_IdAndLocation_Id(relationId,location.getId()).orElseGet(ClientSubclientLocation::new);
        link.setClientSubclient(relation); link.setLocation(location); apply(link,request); link.setActive(true);
        return ClientSubclientLocationResponse.from(links.save(link));
    }
    @Transactional public ClientSubclientLocationResponse update(Long id, ClientSubclientLocationRequest request) {
        ClientSubclientLocation link=links.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"No se encontró la ubicación asociada"));
        Location location=locations.findById(request.locationId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"No se encontró la ubicación"));
        links.findByClientSubclient_IdAndLocation_Id(link.getClientSubclient().getId(), location.getId())
            .filter(existing -> !existing.getId().equals(id))
            .ifPresent(existing -> { throw new ResponseStatusException(HttpStatus.CONFLICT, "La ubicación ya está asociada a esta relación"); });
        link.setLocation(location); apply(link,request); return ClientSubclientLocationResponse.from(links.save(link));
    }
    @Transactional public ClientSubclientLocationResponse changeStatus(Long id, boolean active) { ClientSubclientLocation link=links.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"No se encontró la ubicación asociada")); link.setActive(active); return ClientSubclientLocationResponse.from(links.save(link)); }
    private ClientSubclient findRelation(Long id) { return relations.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"No se encontró la relación cliente-subcliente")); }
    private void apply(ClientSubclientLocation l, ClientSubclientLocationRequest r) { l.setUsageType(r.usageType()); l.setContactName(clean(r.contactName())); l.setContactPhone(clean(r.contactPhone())); l.setOperatingHours(clean(r.operatingHours())); l.setInstructions(clean(r.instructions())); }
    private String clean(String v) { return v == null || v.isBlank() ? null : v.trim(); }
}
