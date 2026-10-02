export interface User {

    username: string;
    email: string | null;
    firstName: string;
    lastName: string;
    documentType: string;
    documentNumber: string;
    phone: string | null;
    role: string;
    active: boolean;
    
}