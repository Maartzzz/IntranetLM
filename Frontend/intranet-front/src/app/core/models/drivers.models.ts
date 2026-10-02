export interface Driver {
    username: string;
    firstName: string;
    lastName: string;
    documentType: string;
    documentNumber: string;
    phone: string | null;
    licenseNumber: string;
    licenseCategory: string;
    licenseIssueDate: string;
    licenseExpiryDate: string;
    active: boolean;
}