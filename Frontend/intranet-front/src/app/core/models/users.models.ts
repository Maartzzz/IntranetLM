export interface UserRole {
  code: string;
  name: string;
}

export interface User {
  username: string;
  email: string | null;
  firstName: string;
  lastName: string;
  documentType: string;
  documentNumber: string;
  phone: string | null;
  role: string;
  roles: UserRole[];
  active: boolean;
}
