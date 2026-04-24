import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { RoleService } from '../../services/role.service';
import { LoadingService } from '../../../../core/services/loading.service';
import { NotificationService } from '../../../../core/services/notification.service';

@Component({
  selector: 'app-role-form',
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
  templateUrl: './role-form.component.html',
  styleUrls: ['./role-form.component.scss']
})
export class RoleFormComponent implements OnInit {
  roleForm: FormGroup;
  isEditMode = false;
  roleId: number | null = null;
  isLoading$ = this.loadingService.isLoading$;

  constructor(
    private fb: FormBuilder,
    private roleService: RoleService,
    private router: Router,
    private route: ActivatedRoute,
    private loadingService: LoadingService,
    private notificationService: NotificationService
  ) {
    this.roleForm = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(100)]],
      description: ['', [Validators.maxLength(500)]]
    });
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.roleId = +id;
      this.loadRole(this.roleId);
    }
  }

  loadRole(id: number): void {
    this.roleService.getRoleById(id).subscribe({
      next: (role) => {
        this.roleForm.patchValue({
          name: role.name,
          description: role.description || ''
        });
      },
      error: (error) => console.error('Error loading role:', error)
    });
  }

  onSubmit(): void {
    if (this.roleForm.valid) {
      const roleData = this.roleForm.value;

      const request$ = this.isEditMode && this.roleId
        ? this.roleService.updateRole(this.roleId, roleData)
        : this.roleService.createRole(roleData);

      request$.subscribe({
        next: () => {
          const message = this.isEditMode 
            ? 'Rol actualizado exitosamente' 
            : 'Rol creado exitosamente';
          this.notificationService.showSuccess(message);
          this.router.navigate(['/roles']);
        },
        error: (error) => console.error('Error saving role:', error)
      });
    }
  }

  onCancel(): void {
    this.router.navigate(['/roles']);
  }
}
