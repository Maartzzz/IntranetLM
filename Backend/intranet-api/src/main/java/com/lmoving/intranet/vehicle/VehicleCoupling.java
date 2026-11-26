package com.lmoving.intranet.vehicle;
import jakarta.persistence.*; import java.time.*;
@Entity @Table(name="vehicle_couplings") public class VehicleCoupling {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(optional=false,fetch=FetchType.LAZY) @JoinColumn(name="tractor_id",nullable=false) private Vehicle tractor;
 @ManyToOne(optional=false,fetch=FetchType.LAZY) @JoinColumn(name="trailer_id",nullable=false) private Vehicle trailer;
 @Column(name="start_at",nullable=false) private LocalDateTime startAt; @Column(name="end_at") private LocalDateTime endAt;
 @Column(nullable=false,length=40) private String reason; @Column(columnDefinition="TEXT") private String observations;
 @Column(name="created_at",nullable=false,updatable=false) private LocalDateTime createdAt;
 @PrePersist void create(){createdAt=LocalDateTime.now();}
 public Long getId(){return id;} public Vehicle getTractor(){return tractor;} public void setTractor(Vehicle v){tractor=v;} public Vehicle getTrailer(){return trailer;} public void setTrailer(Vehicle v){trailer=v;}
 public LocalDateTime getStartAt(){return startAt;} public void setStartAt(LocalDateTime v){startAt=v;} public LocalDateTime getEndAt(){return endAt;} public void setEndAt(LocalDateTime v){endAt=v;}
 public String getReason(){return reason;} public void setReason(String v){reason=v;} public String getObservations(){return observations;} public void setObservations(String v){observations=v;} public LocalDateTime getCreatedAt(){return createdAt;}
}