import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ApplicationService } from '../../services/application.service';
import { RoleService } from '../../../role/services/role.service';
import { LoadingService } from '../../../../core/services/loading.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { Role } from '../../../role/models/role.model';

@Component({
  selector: 'app-application-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatCardModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './application-form.component.html',
  styleUrls: ['./application-form.component.scss']
})
export class ApplicationFormComponent implements OnInit {
  applicationForm: FormGroup;
  isEditMode = false;
  applicationId: number | null = null;
  isLoading$ = this.loadingService.isLoading$;
  roles: Role[] = [];

  constructor(
    private fb: FormBuilder,
    private applicationService: ApplicationService,
    private roleService: RoleService,
    private router: Router,
    private route: ActivatedRoute,
    private loadingService: LoadingService,
    private notificationService: NotificationService
  ) {
    this.applicationForm = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(100)]],
      owner: ['', [Validators.required, Validators.maxLength(100)]],
      url: ['', [Validators.required, Validators.pattern(/^https?:\/\/.+/)]],
      roleId: [null, [Validators.required]]
    });
  }

  ngOnInit(): void {
    this.loadRoles();

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.applicationId = +id;
      this.loadApplication(this.applicationId);
    }
  }

  loadRoles(): void {
    this.roleService.getRoles(0, 1000).subscribe({
      next: (data) => {
        this.roles = data.content;
      },
      error: (error) => console.error('Error loading roles:', error)
    });
  }

  loadApplication(id: number): void {
    this.applicationService.getApplicationById(id).subscribe({
      next: (application) => {
        this.applicationForm.patchValue({
          name: application.name,
          owner: application.owner,
          url: application.url,
          roleId: application.roleId
        });
      },
      error: (error) => console.error('Error loading application:', error)
    });
  }

  onSubmit(): void {
    if (this.applicationForm.valid) {
      const applicationData = this.applicationForm.value;

      const request$ = this.isEditMode && this.applicationId
        ? this.applicationService.updateApplication(this.applicationId, applicationData)
        : this.applicationService.createApplication(applicationData);

      request$.subscribe({
        next: () => {
          const message = this.isEditMode 
            ? 'Aplicación actualizada exitosamente' 
            : 'Aplicación creada exitosamente';
          this.notificationService.showSuccess(message);
          this.router.navigate(['/applications']);
        },
        error: (error) => console.error('Error saving application:', error)
      });
    }
  }

  onCancel(): void {
    this.router.navigate(['/applications']);
  }
}
