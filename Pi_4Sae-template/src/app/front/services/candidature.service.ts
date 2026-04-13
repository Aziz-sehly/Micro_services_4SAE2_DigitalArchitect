import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { FreelancerPreferences } from '../models/models';
import { UserService } from './user.service';

@Injectable({ providedIn: 'root' })
export class CandidatureService {
  private readonly baseUrl = `${environment.apiGateway}/candidature`;

  constructor(
    private readonly http: HttpClient,
    private readonly userService: UserService
  ) {}

  private dtoToPreferences(dto: any): FreelancerPreferences {
    const raw = dto ?? {};
    const skills = Array.isArray(raw.skills) ? raw.skills : (raw.skills ? String(raw.skills).split(',').map((s: string) => s.trim()).filter(Boolean) : []);
    const preferredProjectTypes = Array.isArray(raw.preferredProjectTypes)
      ? raw.preferredProjectTypes
      : (raw.preferredProjectTypes ? String(raw.preferredProjectTypes).split(',').map((s: string) => s.trim()).filter(Boolean) : []);
    return {
      id: raw.id,
      freelancerId: raw.freelancerId != null ? String(raw.freelancerId) : '',
      skills,
      preferredProjectTypes,
      minBudget: Number(raw.minBudget ?? raw.min_budget ?? 0),
      maxBudget: Number(raw.maxBudget ?? raw.max_budget ?? 0),
      avgDeliveryDays: Number(raw.avgDeliveryDays ?? raw.avg_delivery_days ?? 0),
      openToNegotiation: Boolean(raw.openToNegotiation ?? raw.open_to_negotiation ?? false),
      openToOtherProjectTypes: Boolean(raw.openToOtherProjectTypes ?? raw.open_to_other_project_types ?? false),
      notes: raw.notes ?? ''
    };
  }

  getAll(): Observable<FreelancerPreferences[]> {
    return this.http.get<any[]>(`${this.baseUrl}/all`).pipe(
      map((list) => (Array.isArray(list) ? list : []).map((dto) => this.dtoToPreferences(dto))),
      catchError(() => of([]))
    );
  }

  /** Freelancer connecté : GET /candidature/me */
  getMy(): Observable<FreelancerPreferences | null> {
    return this.http.get<any>(`${this.baseUrl}/me`).pipe(
      map((dto) => this.dtoToPreferences(dto)),
      catchError(() => of(null))
    );
  }

  getByKeycloakSubject(subject: string): Observable<FreelancerPreferences | null> {
    if (!subject) return of(null);
    return this.http
      .get<any>(`${this.baseUrl}/by-subject/${encodeURIComponent(subject)}`, { observe: 'response' })
      .pipe(
        map((res) => {
          if (res.status === 204 || res.body == null) return null;
          return this.dtoToPreferences(res.body);
        }),
        catchError(() => of(null))
      );
  }

  /**
   * Résout l’utilisateur .NET puis charge les préférences par keycloakId.
   * (L’ancien GET /candidature/{id} numérique n’existait pas côté API.)
   */
  getByFreelancer(freelancerBackendId: number): Observable<FreelancerPreferences | null> {
    if (!freelancerBackendId) return of(null);
    return this.userService.getById(freelancerBackendId).pipe(
      switchMap((u) => (u?.keycloakId ? this.getByKeycloakSubject(u.keycloakId) : of(null))),
      catchError(() => of(null))
    );
  }

  createMy(preferences: Omit<FreelancerPreferences, 'id' | 'freelancerId'>): Observable<FreelancerPreferences | null> {
    const payload = this.preferencesToPayload(preferences);
    return this.http.post<any>(`${this.baseUrl}/me`, payload).pipe(
      map((dto) => this.dtoToPreferences(dto)),
      catchError(() => of(null))
    );
  }

  updateMy(preferences: Partial<FreelancerPreferences>): Observable<FreelancerPreferences | null> {
    const payload = this.preferencesToPayload(preferences);
    return this.http.put<any>(`${this.baseUrl}/me`, payload).pipe(
      map((dto) => this.dtoToPreferences(dto)),
      catchError(() => of(null))
    );
  }

  deleteMy(): Observable<boolean> {
    return this.http.delete<void>(`${this.baseUrl}/me`).pipe(
      map(() => true),
      catchError(() => of(false))
    );
  }

  /** @deprecated Utiliser createMy */
  create(_freelancerId: number, preferences: Omit<FreelancerPreferences, 'id' | 'freelancerId'>): Observable<FreelancerPreferences | null> {
    return this.createMy(preferences);
  }

  /** @deprecated Utiliser updateMy */
  update(_freelancerId: number, preferences: Partial<FreelancerPreferences>): Observable<FreelancerPreferences | null> {
    return this.updateMy(preferences);
  }

  /** @deprecated Utiliser deleteMy */
  delete(_freelancerId: number): Observable<boolean> {
    return this.deleteMy();
  }

  /** Admin : suppression par sujet Keycloak */
  deleteByAdminSubject(keycloakSubject: string): Observable<boolean> {
    return this.http.delete<void>(`${this.baseUrl}/by-subject/${encodeURIComponent(keycloakSubject)}`).pipe(
      map(() => true),
      catchError(() => of(false))
    );
  }

  private preferencesToPayload(p: Partial<FreelancerPreferences>): any {
    return {
      skills: p.skills ?? [],
      preferredProjectTypes: p.preferredProjectTypes ?? [],
      minBudget: p.minBudget,
      maxBudget: p.maxBudget,
      avgDeliveryDays: p.avgDeliveryDays,
      openToNegotiation: p.openToNegotiation ?? false,
      openToOtherProjectTypes: p.openToOtherProjectTypes ?? false,
      notes: p.notes ?? null
    };
  }
}
