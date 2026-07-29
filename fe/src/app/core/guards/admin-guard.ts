import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

export const adminGuard: CanActivateFn = () => {
  const raw = localStorage.getItem('ff_user');
  if (raw) {
    const user = JSON.parse(raw);
    if (user.role === 'ADMIN') return true;
  }
  return inject(Router).createUrlTree(['/dashboard']);
};
