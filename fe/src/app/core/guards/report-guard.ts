import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

export const reportGuard: CanActivateFn = () => {
  const raw = localStorage.getItem('ff_user');
  if (raw) {
    const user = JSON.parse(raw);
    const role = user.role;
    if (role === 'ADMIN' || role === 'FACTORY_MANAGER' || role === 'AUDITOR') return true;
  }
  return inject(Router).createUrlTree(['/dashboard']);
};
