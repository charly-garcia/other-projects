import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { PageEvent } from '@angular/material/paginator';
import { BehaviorSubject } from 'rxjs';
import { DataTableComponent } from '../../../../shared/components/data-table/data-table.component';
import { ConfirmDialogComponent } from '../../../../shared/components/confirm-dialog/confirm-dialog.component';
import { ColumnDef } from '../../../../core/models/column-def.model';
import { PagedResponse } from '../../../../core/models/paged-response.model';
import { Area } from '../../models/area.model';
import { AreaService } from '../../services/area.service';
import { NotificationService } from '../../../../core/services/notification.service';

@Component({
  selector: 'app-area-list',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    DataTableComponent
  ],
  templateUrl: './area-list.component.html',
  styleUrls: ['./area-list.component.scss']
})
export class AreaListComponent implements OnInit {
  columns: ColumnDef[] = [
    { field: 'name', header: 'Nombre' },
    { field: 'description', header: 'Descripción' }
  ];

  dataSource$ = new BehaviorSubject<PagedResponse<Area>>({
    content: [],
    totalElements: 0,
    totalPages: 0,
    currentPage: 0,
    pageSize: 20
  });

  currentPage = 0;
  pageSize = 20;
  searchTerm = '';

  constructor(
    private areaService: AreaService,
    private router: Router,
    private dialog: MatDialog,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.loadAreas();
  }

  loadAreas(): void {
    this.areaService.getAreas(this.currentPage, this.pageSize, this.searchTerm)
      .subscribe({
        next: (data) => this.dataSource$.next(data),
        error: (error) => console.error('Error loading areas:', error)
      });
  }

  onPageChange(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadAreas();
  }

  onSearchChange(searchTerm: string): void {
    this.searchTerm = searchTerm;
    this.currentPage = 0;
    this.loadAreas();
  }

  onEdit(area: Area): void {
    this.router.navigate(['/areas', area.id, 'edit']);
  }

  onDelete(area: Area): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Confirmar eliminación',
        message: `¿Está seguro de que desea eliminar el área "${area.name}"?`,
        confirmText: 'Eliminar',
        cancelText: 'Cancelar'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.areaService.deleteArea(area.id).subscribe({
          next: () => {
            this.notificationService.showSuccess('Área eliminada exitosamente');
            this.loadAreas();
          },
          error: (error) => console.error('Error deleting area:', error)
        });
      }
    });
  }

  onCreateNew(): void {
    this.router.navigate(['/areas/new']);
  }
}
