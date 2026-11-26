package com.lmoving.intranet.client.controller;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.lmoving.intranet.client.dto.ClientLocationResponse;
import com.lmoving.intranet.client.dto.LocationRequest;
import com.lmoving.intranet.client.service.ClientLocationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
@RestController
@RequestMapping("/api/clients/{clientId}/locations")
public class ClientLocationController {
 private final ClientLocationService service;
 public ClientLocationController(ClientLocationService service){this.service=service;}
 @GetMapping @PreAuthorize("hasAuthority('LOCATIONS.VIEW')") public ResponseEntity<List<ClientLocationResponse>> list(@PathVariable Long clientId){return ResponseEntity.ok(service.list(clientId));}
 @PreAuthorize("hasAuthority('LOCATIONS.CREATE')") @PostMapping public ResponseEntity<ClientLocationResponse> create(@PathVariable Long clientId,@Valid @RequestBody LocationRequest request){return ResponseEntity.status(HttpStatus.CREATED).body(service.create(clientId,request));}
 @PreAuthorize("hasAuthority('LOCATIONS.EDIT')") @PutMapping("/{locationId}") public ResponseEntity<ClientLocationResponse> update(@PathVariable Long clientId,@PathVariable Long locationId,@Valid @RequestBody LocationRequest request){return ResponseEntity.ok(service.update(clientId,locationId,request));}
 @PreAuthorize("hasAuthority('LOCATIONS.DELETE')") @PatchMapping("/{locationId}/status") public ResponseEntity<ClientLocationResponse> status(@PathVariable Long clientId,@PathVariable Long locationId,@Valid @RequestBody StatusRequest request){return ResponseEntity.ok(service.status(clientId,locationId,request.active()));}
 public record StatusRequest(@NotNull Boolean active){}
}
