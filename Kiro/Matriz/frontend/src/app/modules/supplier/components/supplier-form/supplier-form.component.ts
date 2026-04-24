import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { SupplierService } from '../../services/supplier.service';
import { LoadingService } from '../../../../core/services/loading.service';
import { NotificationService } from '../../../../core/services/notification.service';

@Component({
  selector: 'app-supplier-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatCardModule,
    MatCheckboxModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './supplier-form.component.html',
  styleUrls: ['./supplier-form.component.scss']
})
export class SupplierFormComponent implements OnInit {
  supplierForm: FormGroup;
  isEditMode = false;
  supplierId: number | null = null;
  isLoading$ = this.loadingService.isLoading$;

  constructor(
    private fb: FormBuilder,
    private supplierService: SupplierService,
    private router: Router,
    private route: ActivatedRoute,
    private loadingService: LoadingService,
    private notificationService: NotificationService
  ) {
    this.supplierForm = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(100)]],
      compliance: [false, [Validators.required]]
    });
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.supplierId = +id;
      this.loadSupplier(this.supplierId);
    }
  }

  loadSupplier(id: number): void {
    this.supplierService.getSupplierById(id).subscribe({
      next: (supplier) => {
        this.supplierForm.patchValue({
          name: supplier.name,
          compliance: supplier.compliance
        });
      },
      error: (error) => console.error('Error loading supplier:', error)
    });
  }

  onSubmit(): void {
    if (this.supplierForm.valid) {
      const supplierData = this.supplierForm.value;

      const request$ = this.isEditMode && this.supplierId
        ? this.supplierService.updateSupplier(this.supplierId, supplierData)
        : this.supplierService.createSupplier(supplierData);

      request$.subscribe({
        next: () => {
          const message = this.isEditMode 
            ? 'Proveedor actualizado exitosamente' 
            : 'Proveedor creado exitosamente';
          this.notificationService.showSuccess(message);
          this.router.navigate(['/suppliers']);
        },
        error: (error) => console.error('Error saving supplier:', error)
      });
    }
  }

  onCancel(): void {
    this.router.navigate(['/suppliers']);
  }
}
