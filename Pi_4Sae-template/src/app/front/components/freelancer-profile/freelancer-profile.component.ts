import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Experience, Project, ProjectProposal, ProjectStatsDTO, Status } from '../../models/models';
import { ProjectService } from '../../services/project.service';
import { ProjectProposalService } from '../../services/project-proposal.service';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-freelancer-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './freelancer-profile.component.html',
  styleUrls: ['./freelancer-profile.component.scss']
})
export class FreelancerProfileComponent implements OnInit {
  loading = true;
  proposalsLoading = true;
  proposals: ProjectProposal[] = [];

  acceptedProjectsLoading = true;
  acceptedProjects: Project[] = [];

  projects: Project[] = [];
  filteredProjects: Project[] = [];
  categories: string[] = [];
  searchQuery = '';
  filterCategory = '';
  filterExperience: Experience | '' = '';
  filterBudgetMin: number | '' = '';
  filterBudgetMax: number | '' = '';

  editingId: number | null = null;
  editCoverLetter = '';
  editProposedBudget = 0;
  editDeliveryDays = 0;
  saving = false;

  showProposalForm = false;
  selectedProject: Project | null = null;
  propCoverLetter = '';
  propBudget = 0;
  propDeliveryDays = 0;
  submitting = false;

  private readonly freelancerId = 101;

  coverPhotoUrl = '';
  profilePhotoUrl = '';

  // ── Stats avancées ────────────────────────────────────────────────────────
  stats: ProjectStatsDTO | null = null;

  constructor(
    private readonly proposalService: ProjectProposalService,
    private readonly projectService: ProjectService
  ) {}

  ngOnInit(): void {
    this.projectService.getCategories().subscribe((c) => (this.categories = c));
    this.projectService.getStats().subscribe((s) => (this.stats = s));
    this.loadProposals();
    this.loadProjects();
  }

  loadProposals(): void {
    this.proposalsLoading = true;
    this.proposalService.getByFreelancer(this.freelancerId).subscribe((items) => {
      this.proposals = items;
      this.applyProposalFilter();
      this.loadAcceptedProjects();
      this.proposalsLoading = false;
    });
  }

  private loadAcceptedProjects(): void {
    const acceptedProjectIds = Array.from(
      new Set(this.proposals.filter((p) => p.status === 'ACCEPTED').map((p) => p.projectId))
    );
    if (acceptedProjectIds.length === 0) {
      this.acceptedProjects = [];
      this.acceptedProjectsLoading = false;
      return;
    }
    this.acceptedProjectsLoading = true;
    forkJoin(acceptedProjectIds.map((id) => this.projectService.getById(id))).subscribe({
      next: (projects) => {
        this.acceptedProjects = projects.filter(Boolean) as Project[];
        this.acceptedProjectsLoading = false;
      },
      error: () => { this.acceptedProjects = []; this.acceptedProjectsLoading = false; }
    });
  }

  loadProjects(): void {
    this.loading = true;
    this.projectService.searchProjects({ status: Status.OPEN }, 1, 50).subscribe((res) => {
      this.projects = res.items;
      this.filteredProjects = [...this.projects];
      this.loading = false;
    });
  }

  // ── SEARCH via backend ────────────────────────────────────────────────────
  onSearchInput(): void {
    const q = (this.searchQuery || '').trim();
    if (!q) { this.filteredProjects = [...this.projects]; return; }
    this.loading = true;
    this.projectService.search(q).subscribe((projects) => {
      this.filteredProjects = projects.filter((p) => p.status === Status.OPEN);
      this.loading = false;
    });
  }

  // ── FILTER via backend ────────────────────────────────────────────────────
  onFilterChange(): void {
    const noFilters = !this.filterCategory && !this.filterExperience &&
      this.filterBudgetMin === '' && this.filterBudgetMax === '';
    if (noFilters) { this.filteredProjects = [...this.projects]; return; }
    this.loading = true;
    this.projectService.filter({
      category:   this.filterCategory   || undefined,
      status:     Status.OPEN,
      experience: this.filterExperience || undefined,
      budgetMin:  this.filterBudgetMin,
      budgetMax:  this.filterBudgetMax,
    }).subscribe((projects) => { this.filteredProjects = projects; this.loading = false; });
  }

  clearFilters(): void {
    this.searchQuery = '';
    this.filterCategory = '';
    this.filterExperience = '';
    this.filterBudgetMin = '';
    this.filterBudgetMax = '';
    this.filteredProjects = [...this.projects];
  }

  applyFilters(): void { this.filteredProjects = [...this.projects]; }

  // ── helper pour le chart dynamique ───────────────────────────────────────
  getPercent(value: number): number {
    if (!this.stats || this.stats.totalProjects === 0) return 0;
    return Math.round((value / this.stats.totalProjects) * 100);
  }

  proposalSearchQuery = '';
  filteredProposals: ProjectProposal[] = [];

  applyProposalFilter(): void {
    const q = (this.proposalSearchQuery || '').trim().toLowerCase();
    if (!q) { this.filteredProposals = [...this.proposals]; return; }
    this.filteredProposals = this.proposals.filter(
      (p) => String(p.projectId).includes(q) ||
             p.coverLetter.toLowerCase().includes(q) ||
             String(p.proposedBudget).includes(q)
    );
  }

  onProposalSearchInput(): void { this.applyProposalFilter(); }

  experienceOptions = [
    { value: Experience.ENTRY, label: 'Entry' },
    { value: Experience.INTERMEDIATE, label: 'Intermediate' },
    { value: Experience.EXPERT, label: 'Expert' }
  ];

  openProposalForm(project: Project): void {
    this.selectedProject = project;
    this.propCoverLetter = '';
    this.propBudget = project.budgetMin || 0;
    this.propDeliveryDays = 14;
    this.showProposalForm = true;
  }

  closeProposalForm(): void { this.showProposalForm = false; this.selectedProject = null; }

  submitProposal(): void {
    if (!this.selectedProject || !this.propCoverLetter.trim()) return;
    this.submitting = true;

    this.proposalService.create({
      projectId:         this.selectedProject.id,
      freelancerId:      this.freelancerId,
      coverLetter:       this.propCoverLetter.trim(),
      proposedBudget:    this.propBudget,
      deliveryDays:      this.propDeliveryDays,
      estimatedDuration: this.propDeliveryDays ? `${this.propDeliveryDays} day(s)` : ''
    }).subscribe({
      next: () => {
        // ── Envoyer notification email au client (non bloquant) ───────
        this.proposalService.notifyClient({
          projectId:      this.selectedProject!.id,
          proposedBudget: this.propBudget,
          deliveryDays:   this.propDeliveryDays,
          coverLetter:    this.propCoverLetter.trim()
        }).subscribe();
        // ─────────────────────────────────────────────────────────────

        this.submitting = false;
        this.closeProposalForm();
        this.loadProposals();
        this.loadProjects();
      },
      error: () => { this.submitting = false; }
    });
  }

  alreadyProposed(projectId: number): boolean {
    return this.proposals.some((p) => p.projectId === projectId);
  }

  get totalProposals(): number { return this.proposals.length; }
  get acceptedProposals(): number { return this.proposals.filter((p) => p.status === 'ACCEPTED').length; }
  get pendingProposals(): number { return this.proposals.filter((p) => p.status === 'PENDING').length; }

  canEditOrDelete(p: ProjectProposal): boolean { return p.status === 'PENDING'; }

  startEdit(p: ProjectProposal): void {
    this.editingId = p.id;
    this.editCoverLetter = p.coverLetter;
    this.editProposedBudget = p.proposedBudget;
    this.editDeliveryDays = p.deliveryDays ?? (parseInt(String(p.estimatedDuration || '0'), 10) || 0);
  }

  cancelEdit(): void { this.editingId = null; }

  saveEdit(): void {
    if (this.editingId == null) return;
    this.saving = true;
    this.proposalService.update(this.editingId, {
      coverLetter: this.editCoverLetter,
      proposedBudget: this.editProposedBudget,
      deliveryDays: this.editDeliveryDays,
      estimatedDuration: this.editDeliveryDays ? `${this.editDeliveryDays} day(s)` : ''
    }).subscribe({
      next: () => { this.saving = false; this.editingId = null; this.loadProposals(); },
      error: () => { this.saving = false; }
    });
  }

  delete(id: number): void {
    if (!confirm('Delete this proposal?')) return;
    this.proposalService.delete(id).subscribe(() => this.loadProposals());
  }

  scrollTo(sectionId: string): void {
    const el = document.getElementById(sectionId);
    if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }

  get currentDate(): string {
    return new Date().toLocaleDateString('en-US', {
      weekday: 'long', year: 'numeric', month: 'long', day: 'numeric'
    });
  }
}