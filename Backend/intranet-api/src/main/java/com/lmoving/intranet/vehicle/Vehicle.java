package com.lmoving.intranet.vehicle;
import jakarta.persistence.*; import java.time.LocalDateTime;
@Entity @Table(name="vehicles") public class Vehicle {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,length=20,unique=true) private String plate;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private VehicleType type;
 @Column(length=80) private String brand; @Column(length=80) private String model;
 @Column(name="capacity_description",length=120) private String capacityDescription;
 @Column(nullable=false) private boolean active=true;
 @Column(columnDefinition="TEXT") private String observations;
 @Column(name="created_at",nullable=false,updatable=false) private LocalDateTime createdAt;
 @Column(name="updated_at",nullable=false) private LocalDateTime updatedAt;
 @PrePersist void create(){createdAt=updatedAt=LocalDateTime.now();} @PreUpdate void update(){updatedAt=LocalDateTime.now();}
 public Long getId(){return id;} public String getPlate(){return plate;} public void setPlate(String v){plate=v==null?null:v.trim().toUpperCase();}
 public VehicleType getType(){return type;} public void setType(VehicleType v){type=v;} public String getBrand(){return brand;} public void setBrand(String v){brand=v;}
 public String getModel(){return model;} public void setModel(String v){model=v;} public String getCapacityDescription(){return capacityDescription;} public void setCapacityDescription(String v){capacityDescription=v;}
 public boolean isActive(){return active;} public void setActive(boolean v){active=v;} public String getObservations(){return observations;} public void setObservations(String v){observations=v;}
 public LocalDateTime getCreatedAt(){return createdAt;} public LocalDateTime getUpdatedAt(){return updatedAt;}
}