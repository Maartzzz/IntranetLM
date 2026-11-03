export interface OperationActivity {
  id: number;
  operationId: number;
  actorUsername: string;
  actorName: string;
  action: 'CREATED' | 'UPDATED' | 'STATUS_CHANGED' | string;
  entityType: 'OPERACION' | 'REQUISITO' | string;
  entityId: number | null;
  description: string;
  oldValue: string | null;
  newValue: string | null;
  createdAt: string;
}
