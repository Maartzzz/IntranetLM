package com.lmoving.intranet.client.service;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.lmoving.intranet.client.ClientLocation;
import com.lmoving.intranet.client.dto.ClientLocationResponse;
import com.lmoving.intranet.client.dto.LocationRequest;
import com.lmoving.intranet.client.repository.ClientLocationRepository;
import com.lmoving.intranet.client.repository.ClientRepository;
import com.lmoving.intranet.client.repository.LocationRepository;

@Service
public class ClientLocationService {
 private final ClientLocationRepository links; private final ClientRepository clients; private final LocationRepository locations;
 public ClientLocationService(ClientLocationRepository links, ClientRepository clients, LocationRepository locations){this.links=links;this.clients=clients;this.locations=locations;}
 @Transactional(readOnly=true) public List<ClientLocationResponse> list(Long clientId){ensureClient(clientId);return links.findAllByClient_IdOrderByLocation_NameAsc(clientId).stream().map(ClientLocationResponse::from).toList();}
 @Transactional public ClientLocationResponse create(Long clientId, LocationRequest r){var client=ensureClient(clientId);var l=new com.lmoving.intranet.client.Location();apply(l,r);l.setActive(true);l=locations.save(l);var link=new ClientLocation();link.setClient(client);link.setLocation(l);link.setActive(true);return ClientLocationResponse.from(links.save(link));}
 @Transactional public ClientLocationResponse update(Long clientId,Long locationId,LocationRequest r){var link=find(clientId,locationId);apply(link.getLocation(),r);return ClientLocationResponse.from(links.save(link));}
 @Transactional public ClientLocationResponse status(Long clientId,Long locationId,boolean active){var link=find(clientId,locationId);link.setActive(active);return ClientLocationResponse.from(links.save(link));}
 private ClientLocation find(Long c,Long l){return links.findByClient_IdAndLocation_Id(c,l).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"La ubicación no pertenece a este cliente"));}
 private com.lmoving.intranet.client.Client ensureClient(Long id){return clients.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"No se encontró el cliente"));}
 private void apply(com.lmoving.intranet.client.Location l,LocationRequest r){l.setName(r.name().trim());l.setLocationType(r.locationType());l.setAddress(r.address().trim());l.setReference(clean(r.reference()));l.setDistrict(clean(r.district()));l.setProvince(clean(r.province()));l.setDepartment(clean(r.department()));l.setLatitude(r.latitude());l.setLongitude(r.longitude());}
 private String clean(String s){return s==null||s.isBlank()?null:s.trim();}
}
