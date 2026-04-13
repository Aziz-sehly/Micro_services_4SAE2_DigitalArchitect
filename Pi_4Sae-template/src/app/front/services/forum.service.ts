import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ForumPost, ForumReply } from '../models/models';

@Injectable({ providedIn: 'root' })
export class ForumService {
  private base = `${environment.apiGateway}/api/posts`;

  constructor(private http: HttpClient) {}

  /** GET /api/posts — public */
  getAll(): Observable<ForumPost[]> {
    return this.http.get<ForumPost[]>(this.base);
  }

  /** GET /api/posts/:id — public */
  getById(id: number): Observable<ForumPost> {
    return this.http.get<ForumPost>(`${this.base}/${id}`);
  }

  /** POST /api/posts — client only */
  create(post: Partial<ForumPost>): Observable<ForumPost> {
    return this.http.post<ForumPost>(this.base, post);
  }

  /** PUT /api/posts/:id — client only */
  update(id: number, post: Partial<ForumPost>): Observable<ForumPost> {
    return this.http.put<ForumPost>(`${this.base}/${id}`, post);
  }

  /** DELETE /api/posts/:id — admin only */
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  /** GET /api/posts/:id/replies — public */
  getReplies(postId: number): Observable<ForumReply[]> {
    return this.http.get<ForumReply[]>(`${this.base}/${postId}/replies`);
  }

  /** POST /api/posts/:id/replies — freelancer only */
  addReply(postId: number, reply: Partial<ForumReply>): Observable<ForumReply> {
    return this.http.post<ForumReply>(`${this.base}/${postId}/replies`, reply);
  }

  /** DELETE /api/posts/replies/:replyId — admin only */
  deleteReply(replyId: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/replies/${replyId}`);
  }

  /** POST /api/posts/:id/react?emoji=... — client or freelancer */
  addReaction(postId: number, emoji: string): Observable<ForumPost> {
    const params = new HttpParams().set('emoji', emoji);
    return this.http.post<ForumPost>(`${this.base}/${postId}/react`, null, { params });
  }
}
