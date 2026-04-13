import { Injectable } from '@angular/core';

const ACCESS = 'job_board_access_token';
const REFRESH = 'job_board_refresh_token';

@Injectable({ providedIn: 'root' })
export class AuthTokenService {
  getAccessToken(): string | null {
    if (typeof sessionStorage === 'undefined' && typeof localStorage === 'undefined') {
      return null;
    }
    return sessionStorage.getItem(ACCESS) ?? localStorage.getItem(ACCESS);
  }

  setTokens(accessToken: string, refreshToken?: string, rememberMe = false): void {
    const store = rememberMe ? localStorage : sessionStorage;
    const other = rememberMe ? sessionStorage : localStorage;
    other.removeItem(ACCESS);
    other.removeItem(REFRESH);
    store.setItem(ACCESS, accessToken);
    if (refreshToken) {
      store.setItem(REFRESH, refreshToken);
    }
  }

  clear(): void {
    sessionStorage.removeItem(ACCESS);
    sessionStorage.removeItem(REFRESH);
    localStorage.removeItem(ACCESS);
    localStorage.removeItem(REFRESH);
  }
}
