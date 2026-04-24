import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { forkJoin } from 'rxjs';
import { UserService } from '../../services/user.service';
import { AreaService } from '../../../area/services/area.service';
import { CompanyService } from '../../../company/services/company.service';
import { SupplierService } from '../../../supplier/services/supplier.service';
import { ApplicationService } from '../../../application/services/application.service';
import { LoadingService } from '../../../../core/services/loading.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { Area } from '../../../area/models/area.model';
import { Company } from '../../../company/models/company.model';
import { Supplier } from '../../../supplier/models/supplier.model';
import { Application } from '../../../application/models/application.model';
import { Role } from '../../../role/models/role.model';

@Component({
  selector: 'app-user-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatButtonModule,
    MatCardModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './user-form.component.html',
  styleUrls: ['./user-form.component.scss']
})
export class UserFormComponent implements OnInit {
  userForm: FormGroup;
  isEditMode = false;
  userId: number | null = null;
  isLoading$ = this.loadingService.isLoading$;

  areas: Area[] = [];
  companies: Company[] = [];
  suppliers: Supplier[] = [];
  applications: Application[] = [];
  roles: Role[] = [];

  userTypes = ['Interno', 'Practicante', 'Contractor'];
  statuses = ['ACTIVO', 'INACTIVO'];
  scopes = ['PCI', 'ISO', 'General'];
  informationAccessLevels = ['Secreta', 'Confidencial', 'Uso Interno'];

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private areaService: AreaService,
    private companyService: CompanyService,
    private supplierService: SupplierService,
    private applicationService: ApplicationService,
    private router: Router,
    private route: ActivatedRoute,
    private loadingService: LoadingService,
    private notificationService: NotificationService
  ) {
    this.userForm = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(100)]],
      email: ['', [Validators.required, Validators.email, Validators.maxLength(100)]],
      userType: ['', [Validators.required]],
      status: ['', [Validators.required]],
      startDate: ['', [Validators.required]],
      endDate: [''],
      scope: ['', [Validators.required]],
      informationAccess: ['', [Validators.required]],
      position: ['', [Validators.maxLength(100)]],
      manager: ['', [Validators.maxLength(100)]],
      areaId: [null],
      companyId: [null],
      supplierId: [null],
      applicationId: [null],
      roleId: [{ value: null, disabled: true }]
    });
  }

  ngOnInit(): void {
    this.loadCatalogs();
    this.setupCascadingDropdown();

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.userId = +id;
      this.loadUser(this.userId);
    }
  }

  loadCatalogs(): void {
    forkJoin({
      areas: this.areaService.getAreas(0, 1000),
      companies: this.companyService.getCompanies(0, 1000),
      suppliers: this.supplierService.getSuppliers(0, 1000),
      applications: this.applicationService.getApplications(0, 1000)
    }).subscribe({
      next: (data) => {
        this.areas = data.areas.content;
        this.companies = data.companies.content;
        this.suppliers = data.suppliers.content;
        this.applications = data.applications.content;
      },
      error: (error) => console.error('Error loading catalogs:', error)
    });
  }

  setupCascadingDropdown(): void {
    this.userForm.get('applicationId')?.valueChanges.subscribe(applicationId => {
      const roleIdControl = this.userForm.get('roleId');
      
      if (applicationId) {
        // Enable role dropdown and load roles for selected application
        roleIdControl?.enable();
        this.applicationService.getRolesByApplication(applicationId).subscribe({
          next: (roles) => {
            this.roles = roles;
          },
          error: (error) => console.error('Error loading roles:', error)
        });
      } else {
        // Disable role dropdown and clear selection
        roleIdControl?.disable();
        roleIdControl?.setValue(null);
        this.roles = [];
      }
    });
  }

  loadUser(id: number): void {
    this.userService.getUserById(id).subscribe({
      next: (user) => {
        this.userForm.patchValue({
          name: user.name,
          email: user.email,
          userType: user.userType,
          status: user.status,
          startDate: user.startDate,
          endDate: user.endDate || '',
          scope: user.scope,
          informationAccess: user.informationAccess,
          position: user.position || '',
          manager: user.manager || '',
          areaId: user.areaId || null,
          companyId: user.companyId || null,
          supplierId: user.supplierId || null,
          applicationId: user.applicationId || null,
          roleId: user.roleId || null
        });
      },
      error: (error) => console.error('Error loading user:', error)
    });
  }

  onSubmit(): void {
    if (this.userForm.valid) {
      const userData = {
        ...this.userForm.value,
        roleId: this.userForm.get('roleId')?.value // Include roleId even if disabled
      };

      const request$ = this.isEditMode && this.userId
        ? this.userService.updateUser(this.userId, userData)
        : this.userService.createUser(userData);

      request$.subscribe({
        next: () => {
          const message = this.isEditMode 
            ? 'Usuario actualizado exitosamente' 
            : 'Usuario creado exitosamente';
          this.notificationService.showSuccess(message);
          this.router.navigate(['/users']);
        },
        error: (error) => console.error('Error saving user:', error)
      });
    }
  }

  onCancel(): void {
    this.router.navigate(['/users']);
  }
}
