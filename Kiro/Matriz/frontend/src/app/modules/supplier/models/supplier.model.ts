export interface Supplier {
  id: number;
  name: string;
  compliance: boolean;
}

export interface SupplierRequest {
  name: string;
  compliance: boolean;
}
