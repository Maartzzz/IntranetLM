package com.lmoving.intranet.vehicle; import java.util.*; import org.springframework.web.bind.annotation.*; import org.springframework.http.HttpStatus; import org.springframework.security.access.prepost.PreAuthorize;
@RestController @RequestMapping("/api/vehicles") public class VehicleController {private final VehicleService service;public VehicleController(VehicleService s){service=s;}
@GetMapping public List<VehicleDtos.VehicleResponse> list(@RequestParam(defaultValue="false") boolean activeOnly){return service.list(activeOnly);}
@PostMapping @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR','OPERACIONES')") public VehicleDtos.VehicleResponse create(@RequestBody VehicleDtos.VehicleRequest r){return service.save(null,r);}
@PutMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR','OPERACIONES')") public VehicleDtos.VehicleResponse update(@PathVariable Long id,@RequestBody VehicleDtos.VehicleRequest r){return service.save(id,r);}
@PostMapping("/couplings") @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR','OPERACIONES')") public VehicleDtos.CouplingResponse couple(@RequestBody VehicleDtos.CouplingRequest r){return service.couple(r);}
@GetMapping("/couplings") public List<VehicleDtos.CouplingResponse> history(){return service.history();}}
