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
import { User } from '../../models/user.model';
import { UserService } from '../../services/user.service';
import { NotificationService } from '../../../../core/services/notification.service';

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    DataTableComponent
  ],
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.scss']
})
export class UserListComponent implements OnInit {
  columns: ColumnDef[] = [
    { field: 'name', header: 'Nombre' },
    { field: 'email', header: 'Correo' },
    { field: 'userType', header: 'Tipo' },
    { field: 'status', header: 'Estatus' },
    { field: 'areaName', header: 'Área' },
    { field: 'companyName', header: 'Compañía' },
    { field: 'applicationName', header: 'Aplicación' },
    { field: 'roleName', header: 'Rol' }
  ];

  dataSource$ = new BehaviorSubject<PagedResponse<User>>({
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
    private userService: UserService,
    private router: Router,
    private dialog: MatDialog,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.userService.getUsers(this.currentPage, this.pageSize, this.searchTerm)
      .subscribe({
        next: (data) => this.dataSource$.next(data),
        error: (error) => console.error('Error loading users:', error)
      });
  }

  onPageChange(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadUsers();
  }

  onSearchChange(searchTerm: string): void {
    this.searchTerm = searchTerm;
    this.currentPage = 0;
    this.loadUsers();
  }

  onEdit(user: User): void {
    this.router.navigate(['/users', user.id, 'edit']);
  }

  onDelete(user: User): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Confirmar eliminación',
        message: `¿Está seguro de que desea eliminar el usuario "${user.name}"?`,
        confirmText: 'Eliminar',
        cancelText: 'Cancelar'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.userService.deleteUser(user.id).subscribe({
          next: () => {
            this.notificationService.showSuccess('Usuario eliminado exitosamente');
            this.loadUsers();
          },
          error: (error) => console.error('Error deleting user:', error)
        });
      }
    });
  }

  onCreateNew(): void {
    this.router.navigate(['/users/new']);
  }
}
