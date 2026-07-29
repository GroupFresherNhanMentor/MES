import { inject, PLATFORM_ID } from '@angular/core';
import { CanActivateFn } from '@angular/router';
import { isPlatformBrowser } from '@angular/common';

/** Bypass auth for development */
export const authGuard: CanActivateFn = () => true;
