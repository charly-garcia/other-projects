import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { NotificationService } from '../services/notification.service';

export const httpErrorInterceptor: HttpInterceptorFn = (req, next) => {
  const notificationService = inject(NotificationService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      let errorMessage = 'Ha ocurrido un error';

      if (error.error) {
        // Si el error tiene un campo 'message', usarlo
        if (error.error.message) {
          errorMessage = error.error.message;
        }
        // Si el error tiene un campo 'fields' (errores de validación), construir mensaje
        else if (error.error.fields) {
          const fields = error.error.fields;
          const fieldErrors = Object.keys(fields)
            .map(key => `${key}: ${fields[key]}`)
            .join(', ');
          errorMessage = `Errores de validación: ${fieldErrors}`;
        }
      }

      notificationService.showError(errorMessage);
      return throwError(() => error);
    })
  );
};
