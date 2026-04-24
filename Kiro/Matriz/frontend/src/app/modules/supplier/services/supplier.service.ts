import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PagedResponse } from '../../../core/models/paged-response.model';
import { Supplier, SupplierRequest } from '../models/supplier.model';

@Injectable({
  providedIn: 'root'
})
export class SupplierService {
  private readonly apiUrl = '/api/v1/suppliers';

  constructor(private http: HttpClient) {}

  getSuppliers(page: number = 0, size: number = 20, search: string = ''): Observable<PagedResponse<Supplier>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    if (search) {
      params = params.set('search', search);
    }

    return this.http.get<PagedResponse<Supplier>>(this.apiUrl, { params });
  }

  getSupplierById(id: number): Observable<Supplier> {
    return this.http.get<Supplier>(`${this.apiUrl}/${id}`);
  }

  createSupplier(supplier: SupplierRequest): Observable<Supplier> {
    return this.http.post<Supplier>(this.apiUrl, supplier);
  }

  updateSupplier(id: number, supplier: SupplierRequest): Observable<Supplier> {
    return this.http.put<Supplier>(`${this.apiUrl}/${id}`, supplier);
  }

  deleteSupplier(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
