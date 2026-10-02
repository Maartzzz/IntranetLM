export interface LoginRequest {
    username: string;
    password: string;
}

export interface LoginResponse {
    accessToken: string;
    tokenType: string;
}

export interface CurrentUser {
    username: string;
    firstName: string;
    lastName: string;
    role: string;
}