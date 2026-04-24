import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AreaService } from '../../services/area.service';
import { LoadingService } from '../../../../core/services/loading.service';
import { NotificationService } from '../../../../core/services/notification.service';

@Component({
  selector: 'app-area-form',
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
  templateUrl: './area-form.component.html',
  styleUrls: ['./area-form.component.scss']
})
export class AreaFormComponent implements OnInit {
  areaForm: FormGroup;
  isEditMode = false;
  areaId: number | null = null;
  isLoading$ = this.loadingService.isLoading$;

  constructor(
    private fb: FormBuilder,
    private areaService: AreaService,
    private router: Router,
    private route: ActivatedRoute,
    private loadingService: LoadingService,
    private notificationService: NotificationService
  ) {
    this.areaForm = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(100)]],
      description: ['', [Validators.maxLength(500)]]
    });
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.areaId = +id;
      this.loadArea(this.areaId);
    }
  }

  loadArea(id: number): void {
    this.areaService.getAreaById(id).subscribe({
      next: (area) => {
        this.areaForm.patchValue({
          name: area.name,
          description: area.description || ''
        });
      },
      error: (error) => console.error('Error loading area:', error)
    });
  }

  onSubmit(): void {
    if (this.areaForm.valid) {
      const areaData = this.areaForm.value;

      const request$ = this.isEditMode && this.areaId
        ? this.areaService.updateArea(this.areaId, areaData)
        : this.areaService.createArea(areaData);

      request$.subscribe({
        next: () => {
          const message = this.isEditMode 
            ? 'Área actualizada exitosamente' 
            : 'Área creada exitosamente';
          this.notificationService.showSuccess(message);
          this.router.navigate(['/areas']);
        },
        error: (error) => console.error('Error saving area:', error)
      });
    }
  }

  onCancel(): void {
    this.router.navigate(['/areas']);
  }
}
