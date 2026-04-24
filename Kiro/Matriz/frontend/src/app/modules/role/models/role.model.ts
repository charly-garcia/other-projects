export interface Role {
  id: number;
  name: string;
  description?: string;
}

export interface RoleRequest {
  name: string;
  description?: string;
}
