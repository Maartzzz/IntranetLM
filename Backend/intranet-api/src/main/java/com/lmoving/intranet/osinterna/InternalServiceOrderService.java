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
import com.lmoving.intranet.oscliente.ClientServiceOrderFclQuantityRepository;
import com.lmoving.intranet.oscliente.ClientServiceOrderLclQuantityRepository;
import com.lmoving.intranet.status.Status;
import com.lmoving.intranet.status.StatusRepository;
import com.lmoving.intranet.provider.Provider;
import com.lmoving.intranet.provider.repository.ProviderRepository;

import jakarta.persistence.EntityManager;

@Service @Transactional
public class InternalServiceOrderService {
 private static final String PROCESS="OS_INTERNA";
 private final InternalServiceOrderRepository repository;
 private final InternalServiceOrderFclRepository fclRepository;
 private final InternalServiceOrderLclRepository lclRepository;
 private final ClientRepository clientRepository;
 private final ClientServiceOrderRepository clientOrderRepository;
 private final ClientServiceOrderFclQuantityRepository clientFclQuantityRepository;
 private final ClientServiceOrderLclQuantityRepository clientLclQuantityRepository;
 private final LocationRepository locationRepository;
 private final CargoTypeRepository cargoTypeRepository;
 private final StatusRepository statusRepository;
 private final ProviderRepository providerRepository;
 private final EntityManager entityManager;
 private final com.lmoving.intranet.osinterna.schedule.InternalServiceOrderScheduleHistoryService scheduleHistoryService;
 public InternalServiceOrderService(InternalServiceOrderRepository repository, InternalServiceOrderFclRepository fclRepository,
  InternalServiceOrderLclRepository lclRepository, ClientRepository clientRepository,
  ClientServiceOrderRepository clientOrderRepository, ClientServiceOrderFclQuantityRepository clientFclQuantityRepository,
  ClientServiceOrderLclQuantityRepository clientLclQuantityRepository, LocationRepository locationRepository,
  CargoTypeRepository cargoTypeRepository, StatusRepository statusRepository, ProviderRepository providerRepository, EntityManager entityManager,
  com.lmoving.intranet.osinterna.schedule.InternalServiceOrderScheduleHistoryService scheduleHistoryService){
  this.repository=repository; this.fclRepository=fclRepository; this.lclRepository=lclRepository;
  this.clientRepository=clientRepository; this.clientOrderRepository=clientOrderRepository;
  this.clientFclQuantityRepository=clientFclQuantityRepository; this.clientLclQuantityRepository=clientLclQuantityRepository;
  this.locationRepository=locationRepository; this.cargoTypeRepository=cargoTypeRepository; this.statusRepository=statusRepository;
  this.providerRepository=providerRepository; this.entityManager = entityManager; this.scheduleHistoryService = scheduleHistoryService;
 }
 @Transactional(readOnly=true) public List<InternalServiceOrderResponse> listar(Long clientId){
  List<InternalServiceOrder> rows=clientId==null?repository.findAllByOrderByCreatedAtDesc():repository.findAllByClient_IdOrderByCreatedAtDesc(clientId);
  return rows.stream().map(this::toResponse).toList();
 }
 @Transactional(readOnly=true) public InternalServiceOrderResponse obtener(Long id){return toResponse(buscar(id));}
 public InternalServiceOrderResponse crear(InternalServiceOrderRequest request){
  Client client=clienteActivo(request.clientId());
  ClientServiceOrder clientOrder=validarOrdenCliente(request, client);
  validarTipoYDetalles(request);
  validarDistribucion(request, clientOrder, null);
  Location origin=locationActiva(request.originLocationId(),"Origen");
  Location destination=locationActiva(request.destinationLocationId(),"Destino");
  validarUbicaciones(origin,destination);
  Status status=validarEstado(request.statusId());
  Provider provider=resolverProveedor(request.providerId(),null);
  String clientOrderNumber=resolverNumeroOrdenCliente(request.clientOrderNumber(),clientOrder);
  InternalServiceOrder order=new InternalServiceOrder();
  aplicarDatosPrincipales(order,request,client,clientOrder,origin,destination,status,provider,clientOrderNumber);
  InternalServiceOrder saved=repository.saveAndFlush(order);
  entityManager.refresh(saved);
  if (saved.getScheduledDate() != null) scheduleHistoryService.registrar(saved, "PROGRAMACION_INICIAL", null, saved.getScheduledDate(),
   razon(request.scheduleReason(), "Programación inicial al crear la O/S interna"));
  guardarDetalle(saved,request);
  return toResponse(saved);
 }

 public InternalServiceOrderResponse actualizar(Long id, InternalServiceOrderRequest request){
  InternalServiceOrder order=buscar(id);
  java.time.LocalDate previousScheduledDate = order.getScheduledDate();
  String previousStatusName = order.getStatus() == null ? null : order.getStatus().getName();
  if(!order.getServiceType().equals(request.serviceType()))
   throw new IllegalArgumentException("No se puede cambiar el tipo de servicio de una O/S interna existente");
  if(order.getClientServiceOrder()!=null && !order.getClientServiceOrder().getId().equals(request.clientServiceOrderId()))
   clientOrderRepository.findByIdForUpdate(order.getClientServiceOrder().getId()).orElseThrow(()->notFound("O/S del cliente anterior no encontrada"));
  Client client=clienteActivo(request.clientId());
  ClientServiceOrder clientOrder=validarOrdenCliente(request,client);
  validarTipoYDetalles(request);
  validarDistribucion(request, clientOrder, id);
  Location origin=locationActiva(request.originLocationId(),"Origen");
  Location destination=locationActiva(request.destinationLocationId(),"Destino");
  validarUbicaciones(origin,destination);
  Status status=validarEstado(request.statusId());
  Provider provider=resolverProveedor(request.providerId(),order.getProvider());
  String clientOrderNumber=resolverNumeroOrdenCliente(request.clientOrderNumber(),clientOrder);
  aplicarDatosPrincipales(order,request,client,clientOrder,origin,destination,status,provider,clientOrderNumber);
  repository.saveAndFlush(order);
  boolean dateChanged = !java.util.Objects.equals(
      previousScheduledDate,
      order.getScheduledDate()
  );

  boolean wasCancelled = "Cancelada".equalsIgnoreCase(previousStatusName);

  boolean isNowCancelled = order.getStatus() != null
      && "Cancelada".equalsIgnoreCase(order.getStatus().getName());

  // Registrar reactivación
  if (wasCancelled && !isNowCancelled) {
      scheduleHistoryService.registrar(
          order,
          "REACTIVACION",
          previousScheduledDate,
          previousScheduledDate,
          razon(request.scheduleReason(), "Reactivación de O/S interna")
      );
  }

  // Registrar programación inicial o reprogramación
  if (dateChanged) {
      String event = previousScheduledDate == null
          ? "PROGRAMACION_INICIAL"
          : "REPROGRAMACION";

      scheduleHistoryService.registrar(
          order,
          event,
          previousScheduledDate,
          order.getScheduledDate(),
          razon(
              request.scheduleReason(),
              previousScheduledDate == null
                  ? "Programación inicial mediante actualización"
                  : "Reprogramación mediante actualización"
          )
      );
  }
  // Los detalles son progresivos: si se omite el objeto detail, se conserva el detalle actual.
  guardarDetalle(order,request);
  return toResponse(order);
 }

 private Provider resolverProveedor(Long providerId, Provider current){
  if(providerId==null) return null;
  if(current!=null && current.getId().equals(providerId)) return current;
  Provider provider=providerRepository.findById(providerId).orElseThrow(()->notFound("Proveedor no encontrado"));
  if(!provider.isActive()) throw new IllegalArgumentException("No se puede asignar un proveedor inactivo a una O/S interna");
  return provider;
 }
 private Client clienteActivo(Long clientId){
  Client client=clientRepository.findById(clientId).orElseThrow(()->notFound("Cliente no encontrado"));
  if(!client.isActive()) throw new IllegalArgumentException("El cliente está inactivo");
  return client;
 }
 private ClientServiceOrder validarOrdenCliente(InternalServiceOrderRequest request,Client client){
  if(request.clientServiceOrderId()==null) return null;
  ClientServiceOrder clientOrder=clientOrderRepository.findByIdForUpdate(request.clientServiceOrderId()).orElseThrow(()->notFound("O/S del cliente no encontrada"));
  if(!clientOrder.getClient().getId().equals(client.getId())) throw new IllegalArgumentException("La O/S del cliente pertenece a otro cliente");
  if(!clientOrder.getServiceType().equals(request.serviceType())) throw new IllegalArgumentException("El tipo de servicio debe coincidir con la O/S del cliente");
  return clientOrder;
 }
 private String resolverNumeroOrdenCliente(String supplied,ClientServiceOrder clientOrder){
  String normalized=normalizar(supplied);
  if(clientOrder!=null){
   String linked=normalizar(clientOrder.getClientOrderNumber());
   if(normalized!=null && !normalized.equals(linked)) throw new IllegalArgumentException("El número de O/S cliente debe coincidir con la O/S vinculada");
   return linked;
  }
  if(normalized==null) throw new IllegalArgumentException("El número de O/S cliente es obligatorio cuando no se vincula una O/S cliente");
  return normalized;
 }
 private void validarTipoYDetalles(InternalServiceOrderRequest request){
  if(request.fclDetail()!=null && !"FCL".equals(request.serviceType())) throw new IllegalArgumentException("El detalle FCL solo corresponde a una O/S interna FCL");
  if(request.lclDetail()!=null && !"LCL".equals(request.serviceType())) throw new IllegalArgumentException("El detalle LCL solo corresponde a una O/S interna LCL");
 }
 private void validarDistribucion(InternalServiceOrderRequest request, ClientServiceOrder clientOrder, Long excludeId){
  if(clientOrder==null) return; // Las O/S independientes no consumen saldo.
  if("FCL".equals(request.serviceType())){
   Long cargoTypeId=request.fclDetail()!=null?request.fclDetail().cargoTypeId():null;
   if(request.fclDetail()==null && cargoTypeId==null && excludeId!=null) cargoTypeId=fclRepository.findById(excludeId).map(d->d.getCargoType()==null?null:d.getCargoType().getId()).orElse(null);
   if(cargoTypeId==null) return; // Captura progresiva: aún no hay cantidad asignable.
   final Long selectedCargoTypeId=cargoTypeId;
   var requested=clientFclQuantityRepository.findAllByClientServiceOrder_IdOrderByCargoType_NameAsc(clientOrder.getId())
    .stream().filter(q->q.getCargoType().getId().equals(selectedCargoTypeId)).findFirst()
    .orElseThrow(()->new IllegalArgumentException("El tipo de contenedor no está solicitado en la O/S cliente vinculada"));
   long distributed=fclRepository.countDistributedByCargoType(clientOrder.getId(),selectedCargoTypeId,excludeId);
   if(distributed+1>requested.getTotalQuantity())
    throw new IllegalArgumentException("La distribución supera la cantidad solicitada para este tipo de contenedor. Saldo disponible: "+(requested.getTotalQuantity()-distributed));
  } else {
   Integer packages=request.lclDetail()!=null?request.lclDetail().packages():null;
   if(request.lclDetail()==null && packages==null && excludeId!=null) packages=lclRepository.findById(excludeId).map(InternalServiceOrderLcl::getPackages).orElse(null);
   if(packages==null) return; // Los bultos pueden completarse posteriormente.
   var requested=clientLclQuantityRepository.findById(clientOrder.getId())
    .orElseThrow(()->new IllegalArgumentException("La O/S cliente vinculada no tiene cantidad LCL configurada"));
   long distributed=lclRepository.sumDistributedPackages(clientOrder.getId(),excludeId);
   if(distributed+packages>requested.getTotalPackages())
    throw new IllegalArgumentException("La distribución supera la cantidad de bultos solicitada. Saldo disponible: "+(requested.getTotalPackages()-distributed));
  }
 }
 public InternalServiceOrderResponse cancelar(Long id){
  InternalServiceOrder order=buscar(id);
  if(order.getClientServiceOrder()!=null)
   clientOrderRepository.findByIdForUpdate(order.getClientServiceOrder().getId()).orElseThrow(()->notFound("O/S del cliente no encontrada"));
  Status cancelled=statusRepository.findFirstByProcessType_CodeAndNameAndActiveTrue(PROCESS,"Cancelada")
   .orElseThrow(()->new IllegalStateException("No existe el estado Cancelada para OS_INTERNA; verifica la migración V25"));
  String previousStatusName = order.getStatus() == null ? null : order.getStatus().getName();
  order.setStatus(cancelled);
  repository.saveAndFlush(order);
  if (!"Cancelada".equalsIgnoreCase(previousStatusName))
   scheduleHistoryService.registrar(order, "CANCELACION", order.getScheduledDate(), null, "Cancelación de O/S interna");
  return toResponse(order);
 }
 public DistributionResponse obtenerDistribucion(Long clientOrderId){
  ClientServiceOrder clientOrder=clientOrderRepository.findById(clientOrderId).orElseThrow(()->notFound("O/S del cliente no encontrada"));
  if("FCL".equals(clientOrder.getServiceType())){
   var rows=clientFclQuantityRepository.findAllByClientServiceOrder_IdOrderByCargoType_NameAsc(clientOrderId).stream().map(q->{
    long distributed=fclRepository.countDistributedByCargoType(clientOrderId,q.getCargoType().getId(),null);
    return new FclBalance(q.getCargoType().getId(),q.getCargoType().getName(),q.getTotalQuantity(),distributed,(long)q.getTotalQuantity()-distributed);
   }).toList();
   return new DistributionResponse(clientOrderId,clientOrder.getClientOrderNumber(),"FCL",rows,null,null,null);
  }
  var requested=clientLclQuantityRepository.findById(clientOrderId).orElse(null);
  int total=requested==null?0:requested.getTotalPackages();
  long distributed=lclRepository.sumDistributedPackages(clientOrderId,null);
  return new DistributionResponse(clientOrderId,clientOrder.getClientOrderNumber(),"LCL",List.of(),total,distributed,total-(int)distributed);
 }
 public record FclBalance(Long cargoTypeId,String cargoTypeName,Integer requested,long distributed,long available){}
 public record DistributionResponse(Long clientServiceOrderId,String clientOrderNumber,String serviceType,List<FclBalance> fcl,Integer requestedPackages,Long distributedPackages,Integer availablePackages){}
 private void validarUbicaciones(Location origin,Location destination){
  if(origin.getId().equals(destination.getId())) throw new IllegalArgumentException("El origen y destino no pueden ser la misma ubicación");
 }
 private Status validarEstado(Long statusId){
  return statusId==null?null:statusRepository.findByIdAndProcessType_CodeAndActiveTrue(statusId,PROCESS)
   .orElseThrow(()->new IllegalArgumentException("El estado no existe, está inactivo o no pertenece a OS_INTERNA"));
 }
 private void aplicarDatosPrincipales(InternalServiceOrder order,InternalServiceOrderRequest request,Client client,
  ClientServiceOrder clientOrder,Location origin,Location destination,Status status,Provider provider,String clientOrderNumber){
  order.setClient(client); order.setClientServiceOrder(clientOrder); order.setClientOrderNumber(clientOrderNumber);
  order.setServiceType(request.serviceType()); order.setReference(normalizar(request.reference()));
  order.setOrigin(origin); order.setDestination(destination); order.setProvider(provider); order.setScheduledDate(request.scheduledDate());
  order.setStatus(status); order.setBillingStatus(request.billingStatus()==null?"PENDIENTE":request.billingStatus());
  order.setObservations(normalizar(request.observations()));
 }
 private void guardarDetalle(InternalServiceOrder saved,InternalServiceOrderRequest request){
  if(request.fclDetail()!=null){
   InternalServiceOrderRequest.FclDetail d=request.fclDetail();
   InternalServiceOrderFcl detail=fclRepository.findById(saved.getId()).orElseGet(()->new InternalServiceOrderFcl(saved));
   detail.setContainerNumber(normalizar(d.containerNumber())); detail.setMemo(normalizar(d.memo()));
   detail.setCubicMeters(d.cubicMeters()); detail.setReturnDueDate(d.returnDueDate());
   if(d.cargoTypeId()!=null){CargoType type=cargoTypeRepository.findById(d.cargoTypeId()).orElseThrow(()->notFound("Tipo de contenedor no encontrado"));
    if(!type.isActive()||!type.isContainer()) throw new IllegalArgumentException("El tipo seleccionado no es un contenedor activo"); detail.setCargoType(type);
   } else detail.setCargoType(null);
   detail.setEmptyWarehouseLocation(d.emptyWarehouseLocationId()==null?null:locationActiva(d.emptyWarehouseLocationId(),"Almacén de vacíos"));
   fclRepository.save(detail);
  }
  if(request.lclDetail()!=null){
   InternalServiceOrderRequest.LclDetail d=request.lclDetail();
   InternalServiceOrderLcl detail=lclRepository.findById(saved.getId()).orElseGet(()->new InternalServiceOrderLcl(saved));
   detail.setPackages(d.packages()); detail.setWeightKg(d.weightKg()); detail.setCubicMeters(d.cubicMeters()); lclRepository.save(detail);
  }
 }
 private Location locationActiva(Long id,String label){Location l=locationRepository.findById(id).orElseThrow(()->notFound(label+" no encontrado"));
  if(!l.isActive()) throw new IllegalArgumentException(label+" está inactivo"); return l;}
 private InternalServiceOrder buscar(Long id){return repository.findById(id).orElseThrow(()->notFound("O/S interna no encontrada"));}
 private ResponseStatusException notFound(String message){return new ResponseStatusException(HttpStatus.NOT_FOUND,message);}
 private String normalizar(String s){return s==null||s.isBlank()?null:s.trim();}
 private String razon(String supplied, String fallback){return supplied==null||supplied.isBlank()?fallback:supplied.trim();}
 private InternalServiceOrderResponse toResponse(InternalServiceOrder o){
  Object detail=null;
  if("FCL".equals(o.getServiceType())) detail=fclRepository.findById(o.getId()).map(d->new FclResponse(d.getContainerNumber(),d.getCargoType()==null?null:d.getCargoType().getId(),d.getCargoType()==null?null:d.getCargoType().getName(),d.getCubicMeters(),d.getEmptyWarehouseLocation()==null?null:d.getEmptyWarehouseLocation().getId(),d.getReturnDueDate(),d.getMemo())).orElse(null);
  else detail=lclRepository.findById(o.getId()).map(d->new LclResponse(d.getPackages(),d.getWeightKg(),d.getCubicMeters())).orElse(null);
  return new InternalServiceOrderResponse(o.getId(),o.getInternalOrderNumber(),o.getClient().getId(),o.getClient().getBusinessName(),
   o.getClientServiceOrder()==null?null:o.getClientServiceOrder().getId(),o.getClientOrderNumber(),
   o.getServiceType(),o.getReference(),o.getOrigin().getId(),o.getOrigin().getName(),o.getDestination().getId(),o.getDestination().getName(),
   o.getProvider()==null?null:o.getProvider().getId(),o.getProvider()==null?null:o.getProvider().getBusinessName(),o.getScheduledDate(),o.getStatus()==null?null:o.getStatus().getId(),o.getStatus()==null?null:o.getStatus().getName(),
   o.getBillingStatus(),o.getObservations(),detail,o.getCreatedAt(),o.getUpdatedAt());
 }
 public record FclResponse(String containerNumber,Long cargoTypeId,String cargoTypeName,java.math.BigDecimal cubicMeters,Long emptyWarehouseLocationId,java.time.LocalDate returnDueDate,String memo){}
 public record LclResponse(Integer packages,java.math.BigDecimal weightKg,java.math.BigDecimal cubicMeters){}
}
