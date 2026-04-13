import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

/** Redirige vers le bon écran de compte selon le rôle Keycloak (client / freelancer). */
@Component({
  selector: 'app-account-redirect',
  standalone: true,
  template: '<p style="padding:2rem;text-align:center;color:#64748b">Redirection vers ton espace…</p>',
})
export class AccountRedirectComponent implements OnInit {
  constructor(
    private readonly router: Router,
    private readonly auth: AuthService
  ) {}

  ngOnInit(): void {
    const u = this.auth.getCurrentUser();
    if (!u) {
      void this.router.navigate(['/front/login'], { queryParams: { returnUrl: '/front/account' } });
      return;
    }
    if (u.userType === 'client') {
      void this.router.navigate(['/front/profile-client']);
    } else {
      void this.router.navigate(['/front/profile-freelancer']);
    }
  }
}
