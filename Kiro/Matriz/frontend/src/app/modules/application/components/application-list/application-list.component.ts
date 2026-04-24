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
import { Application } from '../../models/application.model';
import { ApplicationService } from '../../services/application.service';
import { NotificationService } from '../../../../core/services/notification.service';

@Component({
  selector: 'app-application-list',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    DataTableComponent
  ],
  templateUrl: './application-list.component.html',
  styleUrls: ['./application-list.component.scss']
})
export class ApplicationListComponent implements OnInit {
  columns: ColumnDef[] = [
    { field: 'name', header: 'Nombre' },
    { field: 'owner', header: 'Owner' },
    { field: 'url', header: 'URL' },
    { field: 'roleName', header: 'Rol' }
  ];

  dataSource$ = new BehaviorSubject<PagedResponse<Application>>({
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
    private applicationService: ApplicationService,
    private router: Router,
    private dialog: MatDialog,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.loadApplications();
  }

  loadApplications(): void {
    this.applicationService.getApplications(this.currentPage, this.pageSize, this.searchTerm)
      .subscribe({
        next: (data) => this.dataSource$.next(data),
        error: (error) => console.error('Error loading applications:', error)
      });
  }

  onPageChange(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadApplications();
  }

  onSearchChange(searchTerm: string): void {
    this.searchTerm = searchTerm;
    this.currentPage = 0;
    this.loadApplications();
  }

  onEdit(application: Application): void {
    this.router.navigate(['/applications', application.id, 'edit']);
  }

  onDelete(application: Application): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Confirmar eliminación',
        message: `¿Está seguro de que desea eliminar la aplicación "${application.name}"?`,
        confirmText: 'Eliminar',
        cancelText: 'Cancelar'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.applicationService.deleteApplication(application.id).subscribe({
          next: () => {
            this.notificationService.showSuccess('Aplicación eliminada exitosamente');
            this.loadApplications();
          },
          error: (error) => console.error('Error deleting application:', error)
        });
      }
    });
  }

  onCreateNew(): void {
    this.router.navigate(['/applications/new']);
  }
}
