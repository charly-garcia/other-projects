import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PagedResponse } from '../../../core/models/paged-response.model';
import { Application, ApplicationRequest } from '../models/application.model';
import { Role } from '../../role/models/role.model';

@Injectable({
  providedIn: 'root'
})
export class ApplicationService {
  private readonly apiUrl = '/api/v1/applications';

  constructor(private http: HttpClient) {}

  getApplications(page: number = 0, size: number = 20, search: string = ''): Observable<PagedResponse<Application>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    if (search) {
      params = params.set('search', search);
    }

    return this.http.get<PagedResponse<Application>>(this.apiUrl, { params });
  }

  getApplicationById(id: number): Observable<Application> {
    return this.http.get<Application>(`${this.apiUrl}/${id}`);
  }

  createApplication(application: ApplicationRequest): Observable<Application> {
    return this.http.post<Application>(this.apiUrl, application);
  }

  updateApplication(id: number, application: ApplicationRequest): Observable<Application> {
    return this.http.put<Application>(`${this.apiUrl}/${id}`, application);
  }

  deleteApplication(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getRolesByApplication(applicationId: number): Observable<Role[]> {
    return this.http.get<Role[]>(`${this.apiUrl}/${applicationId}/roles`);
  }
}
