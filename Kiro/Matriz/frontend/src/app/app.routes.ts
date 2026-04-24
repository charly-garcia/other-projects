import { Routes } from '@angular/router';
import { RoleListComponent } from './modules/role/components/role-list/role-list.component';
import { RoleFormComponent } from './modules/role/components/role-form/role-form.component';
import { AreaListComponent } from './modules/area/components/area-list/area-list.component';
import { AreaFormComponent } from './modules/area/components/area-form/area-form.component';
import { CompanyListComponent } from './modules/company/components/company-list/company-list.component';
import { CompanyFormComponent } from './modules/company/components/company-form/company-form.component';
import { SupplierListComponent } from './modules/supplier/components/supplier-list/supplier-list.component';
import { SupplierFormComponent } from './modules/supplier/components/supplier-form/supplier-form.component';
import { ApplicationListComponent } from './modules/application/components/application-list/application-list.component';
import { ApplicationFormComponent } from './modules/application/components/application-form/application-form.component';
import { UserListComponent } from './modules/user/components/user-list/user-list.component';
import { UserFormComponent } from './modules/user/components/user-form/user-form.component';

export const routes: Routes = [
  { path: '', redirectTo: '/roles', pathMatch: 'full' },
  { path: 'roles', component: RoleListComponent },
  { path: 'roles/new', component: RoleFormComponent },
  { path: 'roles/:id/edit', component: RoleFormComponent },
  { path: 'areas', component: AreaListComponent },
  { path: 'areas/new', component: AreaFormComponent },
  { path: 'areas/:id/edit', component: AreaFormComponent },
  { path: 'companies', component: CompanyListComponent },
  { path: 'companies/new', component: CompanyFormComponent },
  { path: 'companies/:id/edit', component: CompanyFormComponent },
  { path: 'suppliers', component: SupplierListComponent },
  { path: 'suppliers/new', component: SupplierFormComponent },
  { path: 'suppliers/:id/edit', component: SupplierFormComponent },
  { path: 'applications', component: ApplicationListComponent },
  { path: 'applications/new', component: ApplicationFormComponent },
  { path: 'applications/:id/edit', component: ApplicationFormComponent },
  { path: 'users', component: UserListComponent },
  { path: 'users/new', component: UserFormComponent },
  { path: 'users/:id/edit', component: UserFormComponent }
];
