export interface Application {
  id: number;
  name: string;
  owner: string;
  url: string;
  roleId: number;
  roleName: string;
}

export interface ApplicationRequest {
  name: string;
  owner: string;
  url: string;
  roleId: number;
}
