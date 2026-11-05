package com.lmoving.intranet.osinterna.schedule;

import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.lmoving.intranet.osinterna.schedule.dto.ScheduleVersionDetailResponse;
import com.lmoving.intranet.osinterna.schedule.dto.ScheduleVersionSummaryResponse;

@RestController
@RequestMapping("/api/internal-service-orders/schedule-versions")
public class InternalServiceOrderScheduleVersionController {
 private final InternalServiceOrderScheduleVersionService service;
 public InternalServiceOrderScheduleVersionController(InternalServiceOrderScheduleVersionService service){this.service=service;}
 @PostMapping
 @ResponseStatus(HttpStatus.CREATED)
 @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR','OPERACIONES')")
 public ScheduleVersionSummaryResponse guardar(@RequestParam LocalDate startDate,@RequestParam LocalDate endDate) {
  return service.guardar(startDate,endDate);
 }
 @GetMapping
 public List<ScheduleVersionSummaryResponse> listar(@RequestParam LocalDate startDate,@RequestParam LocalDate endDate) {
  return service.listar(startDate,endDate);
 }
 @GetMapping("/{versionId}")
 public ScheduleVersionDetailResponse obtener(@PathVariable Long versionId){return service.obtener(versionId);}
}
