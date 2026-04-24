export interface User {
  id: number;
  name: string;
  email: string;
  userType: string;
  status: string;
  startDate: string;
  endDate?: string;
  scope: string;
  informationAccess: string;
  position?: string;
  manager?: string;
  areaId?: number;
  areaName?: string;
  companyId?: number;
  companyName?: string;
  supplierId?: number;
  supplierName?: string;
  applicationId?: number;
  applicationName?: string;
  roleId?: number;
  roleName?: string;
}

export interface UserRequest {
  name: string;
  email: string;
  userType: string;
  status: string;
  startDate: string;
  endDate?: string;
  scope: string;
  informationAccess: string;
  position?: string;
  manager?: string;
  areaId?: number;
  companyId?: number;
  supplierId?: number;
  applicationId?: number;
  roleId?: number;
}
