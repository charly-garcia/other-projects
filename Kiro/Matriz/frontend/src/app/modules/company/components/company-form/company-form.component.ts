import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { CompanyService } from '../../services/company.service';
import { LoadingService } from '../../../../core/services/loading.service';
import { NotificationService } from '../../../../core/services/notification.service';

@Component({
  selector: 'app-company-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatCardModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './company-form.component.html',
  styleUrls: ['./company-form.component.scss']
})
export class CompanyFormComponent implements OnInit {
  companyForm: FormGroup;
  isEditMode = false;
  companyId: number | null = null;
  isLoading$ = this.loadingService.isLoading$;

  constructor(
    private fb: FormBuilder,
    private companyService: CompanyService,
    private router: Router,
    private route: ActivatedRoute,
    private loadingService: LoadingService,
    private notificationService: NotificationService
  ) {
    this.companyForm = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(100)]],
      country: ['', [Validators.required, Validators.maxLength(100)]]
    });
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.companyId = +id;
      this.loadCompany(this.companyId);
    }
  }

  loadCompany(id: number): void {
    this.companyService.getCompanyById(id).subscribe({
      next: (company) => {
        this.companyForm.patchValue({
          name: company.name,
          country: company.country
        });
      },
      error: (error) => console.error('Error loading company:', error)
    });
  }

  onSubmit(): void {
    if (this.companyForm.valid) {
      const companyData = this.companyForm.value;

      const request$ = this.isEditMode && this.companyId
        ? this.companyService.updateCompany(this.companyId, companyData)
        : this.companyService.createCompany(companyData);

      request$.subscribe({
        next: () => {
          const message = this.isEditMode 
            ? 'Compañía actualizada exitosamente' 
            : 'Compañía creada exitosamente';
          this.notificationService.showSuccess(message);
          this.router.navigate(['/companies']);
        },
        error: (error) => console.error('Error saving company:', error)
      });
    }
  }

  onCancel(): void {
    this.router.navigate(['/companies']);
  }
}
