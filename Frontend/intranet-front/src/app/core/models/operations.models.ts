export interface Operation {
  id: number;
  operationNumber: string;
  reference: string | null;
  clientId: number;
  clientName: string;
  cargoTypeId: number;
  cargoTypeName: string;
  container: boolean;
  customerOrderNumber: string | null;
  origin: string | null;
  destination: string | null;
  quantity: number | null;
  estimatedDate: string | null;
  statusId: number | null;
  statusName: string | null;
  statusColor: string | null;
  observation: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface OperationRequest {
  reference: string | null;
  clientId: number;
  cargoTypeId: number;
  customerOrderNumber: string | null;
  origin: string | null;
  destination: string | null;
  quantity: number | null;
  estimatedDate: string | null;
  statusId: number | null;
  observation: string | null;
}

export interface RequirementCatalog {
  id: number;
  name: string;
  description: string | null;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface OperationRequirement {
  id: number;
  operationId: number;
  requirementCatalogId: number | null;
  requirementName: string;
  externalResponsible: string | null;
  statusId: number;
  statusName: string;
  statusColor: string | null;
  comment: string | null;
  registeredAt: string;
  updatedAt: string;
}

export interface OperationRequirementRequest {
  requirementCatalogId: number | null;
  customName: string | null;
  externalResponsible: string | null;
  statusId: number | null;
  comment: string | null;
}

export interface Operation {
  id: number;
  operationNumber: string;
  reference: string | null;
  clientId: number;
  clientName: string;
  cargoTypeId: number;
  cargoTypeName: string;
  container: boolean;
  customerOrderNumber: string | null;
  origin: string | null;
  destination: string | null;
  quantity: number | null;
  estimatedDate: string | null;
  statusId: number | null;
  statusName: string | null;
  statusColor: string | null;
  observation: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface OperationRequest {
  reference: string | null;
  clientId: number;
  cargoTypeId: number;
  customerOrderNumber: string | null;
  origin: string | null;
  destination: string | null;
  quantity: number | null;
  estimatedDate: string | null;
  statusId: number | null;
  observation: string | null;
}

export interface RequirementCatalog {
  id: number;
  name: string;
  description: string | null;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface OperationRequirement {
  id: number;
  operationId: number;
  requirementCatalogId: number | null;
  requirementName: string;
  externalResponsible: string | null;
  statusId: number;
  statusName: string;
  statusColor: string | null;
  comment: string | null;
  registeredAt: string;
  updatedAt: string;
}

export interface OperationRequirementRequest {
  requirementCatalogId: number | null;
  customName: string | null;
  externalResponsible: string | null;
  statusId: number | null;
  comment: string | null;
}

export interface Status {
  id: number;
  name: string;
  description: string | null;
  color: string | null;
  active: boolean;
}

export interface RequirementCatalogRequest {
  name: string;
  description: string | null;
  active: boolean;
}