import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PagedResponse } from '../../../core/models/paged-response.model';
import { Role, RoleRequest } from '../models/role.model';

@Injectable({
  providedIn: 'root'
})
export class RoleService {
  private readonly apiUrl = '/api/v1/roles';

  constructor(private http: HttpClient) {}

  getRoles(page: number = 0, size: number = 20, search: string = ''): Observable<PagedResponse<Role>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    if (search) {
      params = params.set('search', search);
    }

    return this.http.get<PagedResponse<Role>>(this.apiUrl, { params });
  }

  getRoleById(id: number): Observable<Role> {
    return this.http.get<Role>(`${this.apiUrl}/${id}`);
  }

  createRole(role: RoleRequest): Observable<Role> {
    return this.http.post<Role>(this.apiUrl, role);
  }

  updateRole(id: number, role: RoleRequest): Observable<Role> {
    return this.http.put<Role>(`${this.apiUrl}/${id}`, role);
  }

  deleteRole(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
