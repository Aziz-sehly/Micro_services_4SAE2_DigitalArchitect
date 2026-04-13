import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/** Page réservée aux clients (profil client, jalons client, etc.) */
export const clientOnlyGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const u = auth.getCurrentUser();
  if (!u) {
    return router.createUrlTree(['/front/login'], { queryParams: { returnUrl: state.url } });
  }
  if (u.userType !== 'client') {
    return router.createUrlTree(['/front/profile-freelancer']);
  }
  return true;
};

/** Page réservée aux freelancers */
export const freelancerOnlyGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const u = auth.getCurrentUser();
  if (!u) {
    return router.createUrlTree(['/front/login'], { queryParams: { returnUrl: state.url } });
  }
  if (u.userType !== 'freelancer') {
    return router.createUrlTree(['/front/profile-client']);
  }
  return true;
};
