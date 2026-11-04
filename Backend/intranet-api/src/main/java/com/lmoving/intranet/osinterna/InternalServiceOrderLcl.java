package com.lmoving.intranet.osinterna;
import java.math.BigDecimal;
import jakarta.persistence.*;
@Entity @Table(name="internal_service_order_lcl")
public class InternalServiceOrderLcl {
 @Id @Column(name="internal_service_order_id") private Long id;
 @MapsId @OneToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="internal_service_order_id") private InternalServiceOrder order;
 private Integer packages;
 @Column(name="weight_kg", precision=12, scale=3) private BigDecimal weightKg;
 @Column(name="cubic_meters", precision=12, scale=3) private BigDecimal cubicMeters;
 public InternalServiceOrderLcl(){} public InternalServiceOrderLcl(InternalServiceOrder order){this.order=order;}
 public Long getId(){return id;} public InternalServiceOrder getOrder(){return order;}
 public Integer getPackages(){return packages;} public void setPackages(Integer v){packages=v;}
 public BigDecimal getWeightKg(){return weightKg;} public void setWeightKg(BigDecimal v){weightKg=v;}
 public BigDecimal getCubicMeters(){return cubicMeters;} public void setCubicMeters(BigDecimal v){cubicMeters=v;}
}
