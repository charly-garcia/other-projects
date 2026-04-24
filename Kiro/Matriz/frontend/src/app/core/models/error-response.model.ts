export interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
}

export interface ValidationErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  fields: { [key: string]: string };
  path: string;
}
