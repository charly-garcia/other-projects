import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { FormsModule } from '@angular/forms';
import { Observable } from 'rxjs';
import { ColumnDef } from '../../../core/models/column-def.model';
import { PagedResponse } from '../../../core/models/paged-response.model';

@Component({
  selector: 'app-data-table',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    FormsModule
  ],
  templateUrl: './data-table.component.html',
  styleUrls: ['./data-table.component.scss']
})
export class DataTableComponent<T> implements OnInit {
  @Input() columns: ColumnDef[] = [];
  @Input() dataSource$!: Observable<PagedResponse<T>>;
  @Input() pageSize: number = 20;
  @Input() searchable: boolean = true;

  @Output() pageChange = new EventEmitter<PageEvent>();
  @Output() searchChange = new EventEmitter<string>();
  @Output() editAction = new EventEmitter<T>();
  @Output() deleteAction = new EventEmitter<T>();

  displayedColumns: string[] = [];
  searchTerm: string = '';
  dataSource: PagedResponse<T> | null = null;

  ngOnInit(): void {
    this.displayedColumns = [...this.columns.map(col => col.field), 'actions'];
    
    this.dataSource$.subscribe(data => {
      this.dataSource = data;
    });
  }

  onPageChange(event: PageEvent): void {
    this.pageChange.emit(event);
  }

  onSearch(): void {
    this.searchChange.emit(this.searchTerm);
  }

  onEdit(row: T): void {
    this.editAction.emit(row);
  }

  onDelete(row: T): void {
    this.deleteAction.emit(row);
  }

  getColumnValue(row: any, field: string): any {
    return row[field];
  }
}
