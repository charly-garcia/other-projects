export interface Area {
  id: number;
  name: string;
  description?: string;
}

export interface AreaRequest {
  name: string;
  description?: string;
}
