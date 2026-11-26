import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
export interface FleetVehicle { id:number; plate:string; type:'FURGON'|'PICKUP'|'TRACTO'|'CARRETA'; brand:string|null; model:string|null; capacityDescription:string|null; active:boolean; currentTrailerId?:number|null; currentTrailerPlate?:string|null; }
@Injectable({providedIn:'root'}) export class VehiclesService { private http=inject(HttpClient); listActive():Observable<FleetVehicle[]>{return this.http.get<FleetVehicle[]>('/api/vehicles?activeOnly=true');} }
