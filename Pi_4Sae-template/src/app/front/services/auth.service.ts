import { Injectable, inject } from '@angular/core';
import { BehaviorSubject, Observable, from, of } from 'rxjs';
import { switchMap, tap } from 'rxjs/operators';
import { AuthTokenService } from '../../core/auth-token.service';
import { KeycloakPasswordAuthService } from '../../core/keycloak-password-auth.service';
import { UserService } from './user.service';
import { User, UserCreateRequest } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly apiTokens = inject(AuthTokenService);
  private readonly keycloakAuth = inject(KeycloakPasswordAuthService);
  private readonly userApi = inject(UserService);
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor() {
    if (typeof localStorage === 'undefined') {
      return;
    }
    try {
      const storedUser = localStorage.getItem('currentUser');
      if (storedUser) {
        this.currentUserSubject.next(JSON.parse(storedUser) as User);
      }
    } catch {
      localStorage.removeItem('currentUser');
    }
  }

  /**
   * Register via .NET user microservice, then auto-login via Keycloak.
   * The .NET service creates the user in both PostgreSQL and Keycloak.
   */
  register(data: { firstName: string; lastName: string; email: string; password: string; userType: 'freelancer' | 'client' }): Observable<User> {
    const roleMap = { freelancer: 0, client: 1 };
    const req: UserCreateRequest = {
      firstName: data.firstName,
      lastName: data.lastName,
      email: data.email,
      password: data.password,
      role: roleMap[data.userType],
    };
    return this.userApi.register(req).pipe(
      switchMap(() =>
        from(this.keycloakAuth.login(data.email, data.password, false)).pipe(
          tap(() => {
            const token = this.apiTokens.getAccessToken();
            if (token) {
              this.applyKeycloakSession(token);
            }
          }),
          switchMap(() => {
            const user = this.currentUserSubject.value;
            if (!user) {
              throw new Error('Session not applied after registration');
            }
            return of(user);
          })
        )
      )
    );
  }

  /** After Keycloak login: populate the UI user from the JWT, then resolve the numeric backendId. */
  applyKeycloakSession(accessToken: string): void {
    try {
      const p = this.decodeJwt(accessToken);
      const email = String(p['email'] ?? p['preferred_username'] ?? '');
      const sub = String(p['sub'] ?? '');
      const roles = (p['realm_access'] as { roles?: string[] } | undefined)?.roles ?? [];
      const lower = roles.map((r) => r.toLowerCase());
      const userType: User['userType'] = lower.includes('client')
        ? 'client'
        : lower.includes('freelancer')
          ? 'freelancer'
          : 'freelancer';
      const user: User = {
        id: sub,
        email: email || 'user@local',
        firstName: String(p['given_name'] ?? (email ? email.split('@')[0] : 'User')),
        lastName: String(p['family_name'] ?? ''),
        userType,
        createdAt: new Date(),
      };
      localStorage.setItem('currentUser', JSON.stringify(user));
      this.currentUserSubject.next(user);

      this.resolveBackendId(email);
    } catch (e) {
      console.error('applyKeycloakSession', e);
    }
  }

  /** Fetch the numeric backend user ID from the .NET microservice. */
  private resolveBackendId(email: string): void {
    if (!email) return;
    this.userApi.getByEmail(email).subscribe({
      next: (bu) => {
        const current = this.currentUserSubject.value;
        if (current) {
          current.backendId = bu.id;
          current.profileIncomplete = false;
          current.profileImage = bu.profilePicture ?? current.profileImage;
          localStorage.setItem('currentUser', JSON.stringify(current));
          this.currentUserSubject.next({ ...current });
        }
      },
      error: () => {
        const current = this.currentUserSubject.value;
        if (current) {
          current.profileIncomplete = true;
          localStorage.setItem('currentUser', JSON.stringify(current));
          this.currentUserSubject.next({ ...current });
        }
      },
    });
  }

  private decodeJwt(token: string): Record<string, unknown> {
    const part = token.split('.')[1];
    if (!part) {
      throw new Error('JWT invalide');
    }
    const b64 = part.replace(/-/g, '+').replace(/_/g, '/');
    const pad = '='.repeat((4 - (b64.length % 4)) % 4);
    const json = atob(b64 + pad);
    return JSON.parse(json) as Record<string, unknown>;
  }

  logout(): void {
    localStorage.removeItem('currentUser');
    this.currentUserSubject.next(null);
    this.apiTokens.clear();
  }

  isAuthenticated(): boolean {
    return this.currentUserSubject.value !== null;
  }

  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }
}
