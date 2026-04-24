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
import { Role } from '../../models/role.model';
import { RoleService } from '../../services/role.service';
import { NotificationService } from '../../../../core/services/notification.service';

@Component({
  selector: 'app-role-list',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    DataTableComponent
  ],
  templateUrl: './role-list.component.html',
  styleUrls: ['./role-list.component.scss']
})
export class RoleListComponent implements OnInit {
  columns: ColumnDef[] = [
    { field: 'name', header: 'Nombre' },
    { field: 'description', header: 'Descripción' }
  ];

  dataSource$ = new BehaviorSubject<PagedResponse<Role>>({
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
    private roleService: RoleService,
    private router: Router,
    private dialog: MatDialog,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.loadRoles();
  }

  loadRoles(): void {
    this.roleService.getRoles(this.currentPage, this.pageSize, this.searchTerm)
      .subscribe({
        next: (data) => this.dataSource$.next(data),
        error: (error) => console.error('Error loading roles:', error)
      });
  }

  onPageChange(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadRoles();
  }

  onSearchChange(searchTerm: string): void {
    this.searchTerm = searchTerm;
    this.currentPage = 0;
    this.loadRoles();
  }

  onEdit(role: Role): void {
    this.router.navigate(['/roles', role.id, 'edit']);
  }

  onDelete(role: Role): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Confirmar eliminación',
        message: `¿Está seguro de que desea eliminar el rol "${role.name}"?`,
        confirmText: 'Eliminar',
        cancelText: 'Cancelar'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.roleService.deleteRole(role.id).subscribe({
          next: () => {
            this.notificationService.showSuccess('Rol eliminado exitosamente');
            this.loadRoles();
          },
          error: (error) => console.error('Error deleting role:', error)
        });
      }
    });
  }

  onCreateNew(): void {
    this.router.navigate(['/roles/new']);
  }
}
