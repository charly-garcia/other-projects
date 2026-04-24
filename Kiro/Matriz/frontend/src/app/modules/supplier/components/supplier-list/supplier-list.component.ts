import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { PageEvent } from '@angular/material/paginator';
import { BehaviorSubject, map } from 'rxjs';
import { DataTableComponent } from '../../../../shared/components/data-table/data-table.component';
import { ConfirmDialogComponent } from '../../../../shared/components/confirm-dialog/confirm-dialog.component';
import { ColumnDef } from '../../../../core/models/column-def.model';
import { PagedResponse } from '../../../../core/models/paged-response.model';
import { Supplier } from '../../models/supplier.model';
import { SupplierService } from '../../services/supplier.service';
import { NotificationService } from '../../../../core/services/notification.service';

@Component({
  selector: 'app-supplier-list',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    DataTableComponent
  ],
  templateUrl: './supplier-list.component.html',
  styleUrls: ['./supplier-list.component.scss']
})
export class SupplierListComponent implements OnInit {
  columns: ColumnDef[] = [
    { field: 'name', header: 'Nombre' },
    { field: 'complianceText', header: 'En Cumplimiento' }
  ];

  dataSource$ = new BehaviorSubject<PagedResponse<any>>({
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
    private supplierService: SupplierService,
    private router: Router,
    private dialog: MatDialog,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.loadSuppliers();
  }

  loadSuppliers(): void {
    this.supplierService.getSuppliers(this.currentPage, this.pageSize, this.searchTerm)
      .subscribe({
        next: (data) => {
          // Transform compliance boolean to text for display
          const transformedData = {
            ...data,
            content: data.content.map(supplier => ({
              ...supplier,
              complianceText: supplier.compliance ? 'Sí' : 'No'
            }))
          };
          this.dataSource$.next(transformedData);
        },
        error: (error) => console.error('Error loading suppliers:', error)
      });
  }

  onPageChange(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadSuppliers();
  }

  onSearchChange(searchTerm: string): void {
    this.searchTerm = searchTerm;
    this.currentPage = 0;
    this.loadSuppliers();
  }

  onEdit(supplier: Supplier): void {
    this.router.navigate(['/suppliers', supplier.id, 'edit']);
  }

  onDelete(supplier: Supplier): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Confirmar eliminación',
        message: `¿Está seguro de que desea eliminar el proveedor "${supplier.name}"?`,
        confirmText: 'Eliminar',
        cancelText: 'Cancelar'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.supplierService.deleteSupplier(supplier.id).subscribe({
          next: () => {
            this.notificationService.showSuccess('Proveedor eliminado exitosamente');
            this.loadSuppliers();
          },
          error: (error) => console.error('Error deleting supplier:', error)
        });
      }
    });
  }

  onCreateNew(): void {
    this.router.navigate(['/suppliers/new']);
  }
}
