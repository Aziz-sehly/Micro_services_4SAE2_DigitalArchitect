import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { BackendUser, UserCreateRequest, UserUpdateRequest } from '../models/models';

@Injectable({ providedIn: 'root' })
export class UserService {
  private base = `${environment.apiGateway}/user`;

  constructor(private http: HttpClient) {}

  /** POST /user — public (inscription, pas besoin de JWT) */
  register(req: UserCreateRequest): Observable<BackendUser> {
    return this.http.post<BackendUser>(this.base, req);
  }

  /** GET /user/me — profil de l'utilisateur connecté */
  getMe(): Observable<BackendUser> {
    return this.http.get<BackendUser>(`${this.base}/me`);
  }

  /** GET /user/:id */
  getById(id: number): Observable<BackendUser> {
    return this.http.get<BackendUser>(`${this.base}/${id}`);
  }

  /** GET /user/email/:email */
  getByEmail(email: string): Observable<BackendUser> {
    return this.http.get<BackendUser>(`${this.base}/email/${encodeURIComponent(email)}`);
  }

  /** GET /user/by-role/:role — FREELANCER | CLIENT | ADMIN */
  getByRole(role: string): Observable<BackendUser[]> {
    return this.http.get<BackendUser[]>(`${this.base}/by-role/${role}`);
  }

  /** PUT /user/:id */
  update(id: number, req: UserUpdateRequest): Observable<BackendUser> {
    return this.http.put<BackendUser>(`${this.base}/${id}`, req);
  }

  /** DELETE /user/:id — ADMIN only */
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
