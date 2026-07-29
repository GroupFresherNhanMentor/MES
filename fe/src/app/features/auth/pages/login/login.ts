import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  template: '',
})
export class Login {
  private readonly router = inject(Router);
  constructor() {
    void this.router.navigateByUrl('/dashboard');
  }
}
