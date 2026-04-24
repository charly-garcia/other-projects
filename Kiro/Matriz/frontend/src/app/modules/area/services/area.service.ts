import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PagedResponse } from '../../../core/models/paged-response.model';
import { Area, AreaRequest } from '../models/area.model';

@Injectable({
  providedIn: 'root'
})
export class AreaService {
  private readonly apiUrl = '/api/v1/areas';

  constructor(private http: HttpClient) {}

  getAreas(page: number = 0, size: number = 20, search: string = ''): Observable<PagedResponse<Area>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    if (search) {
      params = params.set('search', search);
    }

    return this.http.get<PagedResponse<Area>>(this.apiUrl, { params });
  }

  getAreaById(id: number): Observable<Area> {
    return this.http.get<Area>(`${this.apiUrl}/${id}`);
  }

  createArea(area: AreaRequest): Observable<Area> {
    return this.http.post<Area>(this.apiUrl, area);
  }

  updateArea(id: number, area: AreaRequest): Observable<Area> {
    return this.http.put<Area>(`${this.apiUrl}/${id}`, area);
  }

  deleteArea(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
