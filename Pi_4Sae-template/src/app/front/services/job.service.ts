import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { map } from 'rxjs/operators';
import { Job, JobFilters, SearchResult } from '../models/models';
import { ProjectService } from './project.service';

/**
 * Adapter service: wraps ProjectService to provide the Job interface
 * used by JobListComponent. Projects are the real backend entity.
 */
@Injectable({ providedIn: 'root' })
export class JobService {
  constructor(private projectService: ProjectService) {}

  getCategories(): string[] {
    return [
      'Web Development', 'Mobile Development', 'Backend Development',
      'Frontend Development', 'Full-Stack Development', 'API Development',
      'Database', 'DevOps', 'Cloud Computing', 'Machine Learning',
    ];
  }

  searchJobs(filters: JobFilters, page: number, pageSize: number): Observable<SearchResult<Job>> {
    return this.projectService.getAll(page, pageSize).pipe(
      map((res) => ({
        items: res.items.map((p): Job => ({
          id: String(p.id),
          clientId: String(p.clientId),
          title: p.title,
          description: p.description,
          category: p.category,
          skills: Array.isArray(p.skills) ? p.skills : (p.skills ?? '').split(',').map(s => s.trim()).filter(Boolean),
          budget: { type: 'fixed' as const, amount: p.budgetMax },
          duration: p.duration,
          experienceLevel: (p.experienceLevel?.toLowerCase() ?? 'intermediate') as Job['experienceLevel'],
          status: (p.status?.toLowerCase().replace('_', '-') ?? 'open') as Job['status'],
          postedAt: p.deadline ? new Date(p.deadline) : new Date(),
          proposals: p.proposalsCount ?? 0,
          clientInfo: { name: 'Client', rating: 4.5, totalSpent: 0, location: 'Remote' },
        })),
        total: res.total,
        page: res.page,
        pageSize: res.pageSize,
      }))
    );
  }
}
