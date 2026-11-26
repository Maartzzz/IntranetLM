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
import com.lmoving.intranet.client.repository.ClientLocationRepository;
import com.lmoving.intranet.client.repository.LocationRepository;

@Service
public class ClientSubclientLocationService {
    private final ClientSubclientRepository relations;
    private final LocationRepository locations;
    private final ClientSubclientLocationRepository links;
    private final ClientLocationRepository clientLocations;
    public ClientSubclientLocationService(ClientSubclientRepository relations, LocationRepository locations, ClientSubclientLocationRepository links, ClientLocationRepository clientLocations) { this.relations=relations; this.locations=locations; this.links=links; this.clientLocations=clientLocations; }
    @Transactional(readOnly = true) public List<ClientSubclientLocationResponse> list(Long relationId) { findRelation(relationId); return links.findAllByClientSubclient_IdAndActiveTrueOrderByLocation_NameAsc(relationId).stream().map(ClientSubclientLocationResponse::from).toList(); }
    @Transactional(readOnly = true) public List<ClientSubclientLocationResponse> listAll(Long relationId) { findRelation(relationId); return links.findAllByClientSubclient_IdOrderByLocation_NameAsc(relationId).stream().map(ClientSubclientLocationResponse::from).toList(); }
    @Transactional public ClientSubclientLocationResponse add(Long relationId, ClientSubclientLocationRequest request) {
        ClientSubclient relation=findRelation(relationId);
        Location location=locations.findById(request.locationId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"No se encontró la ubicación"));
        if (!location.isActive()) throw new ResponseStatusException(HttpStatus.CONFLICT,"No se puede asociar una ubicación inactiva");
        assertLocationBelongsToRelation(relation, location.getId());
        ClientSubclientLocation link=links.findByClientSubclient_IdAndLocation_Id(relationId,location.getId()).orElseGet(ClientSubclientLocation::new);
        link.setClientSubclient(relation); link.setLocation(location); apply(link,request); link.setActive(true);
        return ClientSubclientLocationResponse.from(links.save(link));
    }
    @Transactional public ClientSubclientLocationResponse update(Long id, ClientSubclientLocationRequest request) {
        ClientSubclientLocation link=links.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"No se encontró la ubicación asociada"));
        Location location=locations.findById(request.locationId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"No se encontró la ubicación"));
        if (!location.isActive()) throw new ResponseStatusException(HttpStatus.CONFLICT,"No se puede asociar una ubicación inactiva");
        assertLocationBelongsToRelation(link.getClientSubclient(), location.getId());
        links.findByClientSubclient_IdAndLocation_Id(link.getClientSubclient().getId(), location.getId())
            .filter(existing -> !existing.getId().equals(id))
            .ifPresent(existing -> { throw new ResponseStatusException(HttpStatus.CONFLICT, "La ubicación ya está asociada a esta relación"); });
        link.setLocation(location); apply(link,request); return ClientSubclientLocationResponse.from(links.save(link));
    }
    @Transactional public ClientSubclientLocationResponse changeStatus(Long id, boolean active) { ClientSubclientLocation link=links.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"No se encontró la ubicación asociada")); link.setActive(active); return ClientSubclientLocationResponse.from(links.save(link)); }
    private void assertLocationBelongsToRelation(ClientSubclient relation, Long locationId) {
        boolean belongsToClient = clientLocations.findByClient_IdAndLocation_Id(relation.getClient().getId(), locationId)
            .filter(com.lmoving.intranet.client.ClientLocation::isActive).isPresent();
        boolean belongsToRelated = clientLocations.findByClient_IdAndLocation_Id(relation.getSubclient().getId(), locationId)
            .filter(com.lmoving.intranet.client.ClientLocation::isActive).isPresent();
        if (!belongsToClient && !belongsToRelated) throw new ResponseStatusException(HttpStatus.CONFLICT,
            "La ubicación debe pertenecer a una de las dos empresas de esta relación");
    }
    private ClientSubclient findRelation(Long id) { return relations.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"No se encontró la relación cliente-subcliente")); }
    private void apply(ClientSubclientLocation l, ClientSubclientLocationRequest r) { l.setUsageType(r.usageType()); l.setContactName(clean(r.contactName())); l.setContactPhone(clean(r.contactPhone())); l.setOperatingHours(clean(r.operatingHours())); l.setInstructions(clean(r.instructions())); }
    private String clean(String v) { return v == null || v.isBlank() ? null : v.trim(); }
}
