package com.lmoving.intranet.osinterna.schedule;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.lmoving.intranet.osinterna.schedule.dto.ScheduleVersionDetailResponse;
import com.lmoving.intranet.osinterna.schedule.dto.ScheduleVersionSummaryResponse;
import com.lmoving.intranet.user.User;
import com.lmoving.intranet.user.UserRepository;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Service
@Transactional
public class InternalServiceOrderScheduleVersionService {
 private final InternalServiceOrderScheduleService scheduleService;
 private final InternalServiceOrderScheduleVersionRepository versionRepository;
 private final UserRepository userRepository;
 private final ObjectMapper objectMapper;
 public InternalServiceOrderScheduleVersionService(InternalServiceOrderScheduleService scheduleService,
  InternalServiceOrderScheduleVersionRepository versionRepository, UserRepository userRepository, ObjectMapper objectMapper) {
  this.scheduleService=scheduleService; this.versionRepository=versionRepository;
  this.userRepository=userRepository; this.objectMapper=objectMapper;
 }
 public ScheduleVersionSummaryResponse guardar(LocalDate startDate, LocalDate endDate) {
  validarPeriodo(startDate,endDate);
  Authentication auth=SecurityContextHolder.getContext().getAuthentication();
  if(auth==null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName()))
   throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"No se pudo identificar al usuario de la sesión");
  String username=auth.getName();
  User user=userRepository.findByUsername(username).orElseThrow(() ->
   new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Usuario autenticado no encontrado"));
  String fullName=((user.getFirstName()==null?"":user.getFirstName())+" "+
   (user.getLastName()==null?"":user.getLastName())).trim();
  String actorName=fullName.isBlank()?username:fullName;
  var board=scheduleService.obtenerTablero(startDate,endDate);
  List<Map<String, Object>> snapshot = objectMapper.convertValue(
      board.scheduled(),
      new TypeReference<List<Map<String, Object>>>() {}
  );
  Integer nextVersion=versionRepository.findMaxVersionNumber(startDate,endDate)+1;
  var saved=versionRepository.saveAndFlush(new InternalServiceOrderScheduleVersion(
   nextVersion,startDate,endDate,snapshot,username,actorName));
  return toSummary(saved);
 }
 @Transactional(readOnly=true)
 public List<ScheduleVersionSummaryResponse> listar(LocalDate startDate, LocalDate endDate) {
  validarPeriodo(startDate,endDate);
  return versionRepository.findAllByStartDateAndEndDateOrderByVersionNumberDesc(startDate,endDate)
   .stream().map(this::toSummary).toList();
 }
 @Transactional(readOnly=true)
 public ScheduleVersionDetailResponse obtener(Long id) {
  var v=versionRepository.findById(id).orElseThrow(() ->
   new ResponseStatusException(HttpStatus.NOT_FOUND,"Versión de programación no encontrada"));
  return new ScheduleVersionDetailResponse(v.getId(),v.getVersionNumber(),v.getStartDate(),v.getEndDate(),
   v.getCreatedByUsername(),v.getCreatedByName(),v.getCreatedAt(),v.getSnapshot());
 }
 private ScheduleVersionSummaryResponse toSummary(InternalServiceOrderScheduleVersion v) {
  int count=v.getSnapshot()==null?0:v.getSnapshot().size();
  return new ScheduleVersionSummaryResponse(v.getId(),v.getVersionNumber(),v.getStartDate(),v.getEndDate(),
   v.getCreatedByUsername(),v.getCreatedByName(),v.getCreatedAt(),count);
 }
 private void validarPeriodo(LocalDate startDate,LocalDate endDate) {
  if(startDate==null || endDate==null || endDate.isBefore(startDate))
   throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Debe indicar un rango válido: startDate y endDate");
 }
}
