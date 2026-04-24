import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

export interface Notification {
  message: string;
  type: 'error' | 'success';
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private notificationSubject = new BehaviorSubject<Notification | null>(null);
  public notification$: Observable<Notification | null> = this.notificationSubject.asObservable();

  showError(message: string): void {
    this.notificationSubject.next({ message, type: 'error' });
  }

  showSuccess(message: string): void {
    this.notificationSubject.next({ message, type: 'success' });
  }

  clear(): void {
    this.notificationSubject.next(null);
  }
}
