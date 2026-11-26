package com.lmoving.intranet.vehicle; import java.util.*; import org.springframework.web.bind.annotation.*; import org.springframework.http.HttpStatus; import org.springframework.security.access.prepost.PreAuthorize;
@RestController @RequestMapping("/api/vehicles") public class VehicleController {private final VehicleService service;public VehicleController(VehicleService s){service=s;}
@GetMapping @PreAuthorize("hasAuthority('FLEET.VIEW')") public List<VehicleDtos.VehicleResponse> list(@RequestParam(defaultValue="false") boolean activeOnly){return service.list(activeOnly);}
@PostMapping @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasAuthority('FLEET.CREATE')") public VehicleDtos.VehicleResponse create(@RequestBody VehicleDtos.VehicleRequest r){return service.save(null,r);}
@PutMapping("/{id}") @PreAuthorize("hasAuthority('FLEET.EDIT')") public VehicleDtos.VehicleResponse update(@PathVariable Long id,@RequestBody VehicleDtos.VehicleRequest r){return service.save(id,r);}
@PostMapping("/couplings") @PreAuthorize("hasAuthority('FLEET.EDIT')") public VehicleDtos.CouplingResponse couple(@RequestBody VehicleDtos.CouplingRequest r){return service.couple(r);}
@GetMapping("/couplings") @PreAuthorize("hasAuthority('FLEET.VIEW')") public List<VehicleDtos.CouplingResponse> history(){return service.history();}}
