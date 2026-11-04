package com.lmoving.intranet.osinterna;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.lmoving.intranet.cargotype.CargoType;
import com.lmoving.intranet.cargotype.repository.CargoTypeRepository;
import com.lmoving.intranet.client.Client;
import com.lmoving.intranet.client.Location;
import com.lmoving.intranet.client.repository.ClientRepository;
import com.lmoving.intranet.client.repository.LocationRepository;
import com.lmoving.intranet.oscliente.ClientServiceOrder;
import com.lmoving.intranet.oscliente.ClientServiceOrderRepository;
import com.lmoving.intranet.status.Status;
import com.lmoving.intranet.status.StatusRepository;

import jakarta.persistence.EntityManager;

@Service @Transactional
public class InternalServiceOrderService {
 private static final String PROCESS="OS_INTERNA";
 private final InternalServiceOrderRepository repository;
 private final InternalServiceOrderFclRepository fclRepository;
 private final InternalServiceOrderLclRepository lclRepository;
 private final ClientRepository clientRepository;
 private final ClientServiceOrderRepository clientOrderRepository;
 private final LocationRepository locationRepository;
 private final CargoTypeRepository cargoTypeRepository;
 private final StatusRepository statusRepository;
 private final EntityManager entityManager;
 public InternalServiceOrderService(InternalServiceOrderRepository repository, InternalServiceOrderFclRepository fclRepository,
  InternalServiceOrderLclRepository lclRepository, ClientRepository clientRepository,
  ClientServiceOrderRepository clientOrderRepository, LocationRepository locationRepository,
  CargoTypeRepository cargoTypeRepository, StatusRepository statusRepository, EntityManager entityManager){
  this.repository=repository; this.fclRepository=fclRepository; this.lclRepository=lclRepository;
  this.clientRepository=clientRepository; this.clientOrderRepository=clientOrderRepository;
  this.locationRepository=locationRepository; this.cargoTypeRepository=cargoTypeRepository; this.statusRepository=statusRepository;
  this.entityManager = entityManager;
 }
 @Transactional(readOnly=true) public List<InternalServiceOrderResponse> listar(Long clientId){
  List<InternalServiceOrder> rows=clientId==null?repository.findAllByOrderByCreatedAtDesc():repository.findAllByClient_IdOrderByCreatedAtDesc(clientId);
  return rows.stream().map(this::toResponse).toList();
 }
 @Transactional(readOnly=true) public InternalServiceOrderResponse obtener(Long id){return toResponse(buscar(id));}
 public InternalServiceOrderResponse crear(InternalServiceOrderRequest request){
  Client client=clientRepository.findById(request.clientId()).orElseThrow(()->notFound("Cliente no encontrado"));
  if(!client.isActive()) throw new IllegalArgumentException("El cliente está inactivo");
  ClientServiceOrder clientOrder=null;
  if(request.clientServiceOrderId()!=null){
   clientOrder=clientOrderRepository.findById(request.clientServiceOrderId()).orElseThrow(()->notFound("O/S del cliente no encontrada"));
   if(!clientOrder.getClient().getId().equals(client.getId())) throw new IllegalArgumentException("La O/S del cliente pertenece a otro cliente");
   if(!clientOrder.getServiceType().equals(request.serviceType())) throw new IllegalArgumentException("El tipo de servicio debe coincidir con la O/S del cliente");
  }
  if(request.fclDetail()!=null && !"FCL".equals(request.serviceType())) throw new IllegalArgumentException("El detalle FCL solo corresponde a una O/S interna FCL");
  if(request.lclDetail()!=null && !"LCL".equals(request.serviceType())) throw new IllegalArgumentException("El detalle LCL solo corresponde a una O/S interna LCL");
  Location origin=locationActiva(request.originLocationId(),"Origen");
  Location destination=locationActiva(request.destinationLocationId(),"Destino");
  if(origin.getId().equals(destination.getId())) throw new IllegalArgumentException("El origen y destino no pueden ser la misma ubicación");
  Status status=request.statusId()==null?null:statusRepository.findByIdAndProcessType_CodeAndActiveTrue(request.statusId(),PROCESS)
   .orElseThrow(()->new IllegalArgumentException("El estado no existe, está inactivo o no pertenece a OS_INTERNA"));
  InternalServiceOrder order=new InternalServiceOrder();
  order.setClient(client); order.setClientServiceOrder(clientOrder); order.setServiceType(request.serviceType());
  order.setReference(normalizar(request.reference())); order.setOrigin(origin); order.setDestination(destination);
  order.setScheduledDate(request.scheduledDate()); order.setStatus(status);
  order.setBillingStatus(request.billingStatus()==null?"PENDIENTE":request.billingStatus());
  order.setObservations(normalizar(request.observations()));
  InternalServiceOrder saved=repository.saveAndFlush(order);
  entityManager.refresh(saved);
  if(request.fclDetail()!=null){
   InternalServiceOrderRequest.FclDetail d=request.fclDetail(); InternalServiceOrderFcl detail=new InternalServiceOrderFcl(saved);
   detail.setContainerNumber(normalizar(d.containerNumber())); detail.setMemo(normalizar(d.memo()));
   detail.setCubicMeters(d.cubicMeters()); detail.setReturnDueDate(d.returnDueDate());
   if(d.cargoTypeId()!=null){CargoType type=cargoTypeRepository.findById(d.cargoTypeId()).orElseThrow(()->notFound("Tipo de contenedor no encontrado"));
    if(!type.isActive()||!type.isContainer()) throw new IllegalArgumentException("El tipo seleccionado no es un contenedor activo"); detail.setCargoType(type);}
   if(d.emptyWarehouseLocationId()!=null) detail.setEmptyWarehouseLocation(locationActiva(d.emptyWarehouseLocationId(),"Almacén de vacíos"));
   fclRepository.save(detail);
  }
  if(request.lclDetail()!=null){InternalServiceOrderRequest.LclDetail d=request.lclDetail(); InternalServiceOrderLcl detail=new InternalServiceOrderLcl(saved);
   detail.setPackages(d.packages()); detail.setWeightKg(d.weightKg()); detail.setCubicMeters(d.cubicMeters()); lclRepository.save(detail);}
  return toResponse(saved);
 }
 private Location locationActiva(Long id,String label){Location l=locationRepository.findById(id).orElseThrow(()->notFound(label+" no encontrado"));
  if(!l.isActive()) throw new IllegalArgumentException(label+" está inactivo"); return l;}
 private InternalServiceOrder buscar(Long id){return repository.findById(id).orElseThrow(()->notFound("O/S interna no encontrada"));}
 private ResponseStatusException notFound(String message){return new ResponseStatusException(HttpStatus.NOT_FOUND,message);}
 private String normalizar(String s){return s==null||s.isBlank()?null:s.trim();}
 private InternalServiceOrderResponse toResponse(InternalServiceOrder o){
  Object detail=null;
  if("FCL".equals(o.getServiceType())) detail=fclRepository.findById(o.getId()).map(d->new FclResponse(d.getContainerNumber(),d.getCargoType()==null?null:d.getCargoType().getId(),d.getCargoType()==null?null:d.getCargoType().getName(),d.getCubicMeters(),d.getEmptyWarehouseLocation()==null?null:d.getEmptyWarehouseLocation().getId(),d.getReturnDueDate(),d.getMemo())).orElse(null);
  else detail=lclRepository.findById(o.getId()).map(d->new LclResponse(d.getPackages(),d.getWeightKg(),d.getCubicMeters())).orElse(null);
  return new InternalServiceOrderResponse(o.getId(),o.getInternalOrderNumber(),o.getClient().getId(),o.getClient().getBusinessName(),
   o.getClientServiceOrder()==null?null:o.getClientServiceOrder().getId(),o.getClientServiceOrder()==null?null:o.getClientServiceOrder().getClientOrderNumber(),
   o.getServiceType(),o.getReference(),o.getOrigin().getId(),o.getOrigin().getName(),o.getDestination().getId(),o.getDestination().getName(),
   o.getProviderId(),o.getScheduledDate(),o.getStatus()==null?null:o.getStatus().getId(),o.getStatus()==null?null:o.getStatus().getName(),
   o.getBillingStatus(),o.getObservations(),detail,o.getCreatedAt(),o.getUpdatedAt());
 }
 public record FclResponse(String containerNumber,Long cargoTypeId,String cargoTypeName,java.math.BigDecimal cubicMeters,Long emptyWarehouseLocationId,java.time.LocalDate returnDueDate,String memo){}
 public record LclResponse(Integer packages,java.math.BigDecimal weightKg,java.math.BigDecimal cubicMeters){}
}
