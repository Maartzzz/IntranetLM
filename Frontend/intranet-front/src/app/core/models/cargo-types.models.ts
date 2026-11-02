export interface CargoType {
  id: number;
  name: string;
  container: boolean;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CargoTypeRequest {
  name: string;
  isContainer: boolean;
}
