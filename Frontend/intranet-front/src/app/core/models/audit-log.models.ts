export interface AuditLog {
  id: number;
  actorUsername: string;
  actorName: string;
  module: string;
  entityType: string;
  entityId: number | null;
  entityKey: string | null;
  action: string;
  description: string;
  oldValue: string | null;
  newValue: string | null;
  createdAt: string;
}

export interface AuditFilters {
  module?: string;
  entityType?: string;
  entityId?: number;
  entityKey?: string;
}
