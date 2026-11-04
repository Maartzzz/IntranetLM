package com.lmoving.intranet.osinterna;
import java.math.BigDecimal;
import java.time.LocalDate;
import com.lmoving.intranet.cargotype.CargoType;
import com.lmoving.intranet.client.Location;
import jakarta.persistence.*;
@Entity @Table(name = "internal_service_order_fcl")
public class InternalServiceOrderFcl {
 @Id @Column(name="internal_service_order_id") private Long id;
 @MapsId @OneToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="internal_service_order_id") private InternalServiceOrder order;
 @Column(name="container_number", length=40) private String containerNumber;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="cargo_type_id") private CargoType cargoType;
 @Column(name="cubic_meters", precision=10, scale=3) private BigDecimal cubicMeters;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="empty_warehouse_location_id") private Location emptyWarehouseLocation;
 @Column(name="return_due_date") private LocalDate returnDueDate;
 @Column(columnDefinition="TEXT") private String memo;
 public InternalServiceOrderFcl() {}
 public InternalServiceOrderFcl(InternalServiceOrder order) { this.order=order; }
 public Long getId(){return id;} public InternalServiceOrder getOrder(){return order;}
 public String getContainerNumber(){return containerNumber;} public void setContainerNumber(String v){containerNumber=v;}
 public CargoType getCargoType(){return cargoType;} public void setCargoType(CargoType v){cargoType=v;}
 public BigDecimal getCubicMeters(){return cubicMeters;} public void setCubicMeters(BigDecimal v){cubicMeters=v;}
 public Location getEmptyWarehouseLocation(){return emptyWarehouseLocation;} public void setEmptyWarehouseLocation(Location v){emptyWarehouseLocation=v;}
 public LocalDate getReturnDueDate(){return returnDueDate;} public void setReturnDueDate(LocalDate v){returnDueDate=v;}
 public String getMemo(){return memo;} public void setMemo(String v){memo=v;}
}
