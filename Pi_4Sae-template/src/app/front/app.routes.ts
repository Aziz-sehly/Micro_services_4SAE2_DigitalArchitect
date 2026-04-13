import { Routes } from '@angular/router';
import { clientOnlyGuard, freelancerOnlyGuard } from './guards/profile-access.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./front-layout/front-layout.component').then(m => m.FrontLayoutComponent),
    children: [
      {
        path: '',
        loadComponent: () => import('./components/home/home.component').then(m => m.HomeComponent)
      },
      // =========================
      // Projects (gestion projets)
      // =========================
      {
        path: 'projects',
        loadComponent: () => import('./components/project-list/project-list.component').then(m => m.ProjectListComponent)
      },
      {
        path: 'projects/new',
        loadComponent: () => import('./components/project-form/project-form.component').then(m => m.ProjectFormComponent)
      },
      {
        path: 'projects/:id',
        loadComponent: () => import('./components/project-detail/project-detail.component').then(m => m.ProjectDetailComponent)
      },
      {
        path: 'projects/:id/edit',
        loadComponent: () => import('./components/project-form/project-form.component').then(m => m.ProjectFormComponent)
      },
      {
        path: 'my-proposals',
        canActivate: [freelancerOnlyGuard],
        loadComponent: () => import('./components/my-proposals/my-proposals.component').then(m => m.MyProposalsComponent)
      },
      {
        path: 'my-candidatures',
        redirectTo: 'profile-freelancer',
        pathMatch: 'full'
      },
      {
        path: 'login',
        loadComponent: () => import('./components/login/login.component').then(m => m.LoginComponent)
      },
      {
        path: 'register',
        loadComponent: () => import('./components/register/register.component').then(m => m.RegisterComponent)
      },
      {
        path: 'jobs',
        // Compat: ancien chemin -> nouvelle page projects
        loadComponent: () => import('./components/project-list/project-list.component').then(m => m.ProjectListComponent)
      },
      {
        path: 'jobs/:id',
        // Compat: ancien chemin -> nouveau détail project
        loadComponent: () => import('./components/project-detail/project-detail.component').then(m => m.ProjectDetailComponent)
      },
      {
        path: 'post-job',
        // Compat: ancien chemin -> création projet
        loadComponent: () => import('./components/project-form/project-form.component').then(m => m.ProjectFormComponent)
      },
      {
        path: 'my-jobs',
        canActivate: [clientOnlyGuard],
        loadComponent: () => import('./components/my-jobs/my-jobs.component').then(m => m.MyJobsComponent)
      },
      {
        path: 'proposals',
        canActivate: [clientOnlyGuard],
        loadComponent: () => import('./components/proposals/proposals.component').then(m => m.ProposalsComponent)
      },
      {
        path: 'messages',
        loadComponent: () => import('./components/messages/messages.component').then(m => m.MessagesComponent)
      },
      {
        path: 'profile',
        redirectTo: 'account',
        pathMatch: 'full'
      },
      {
        path: 'account',
        loadComponent: () =>
          import('./components/account-redirect/account-redirect.component').then((m) => m.AccountRedirectComponent),
      },
      {
        path: 'profile-client',
        canActivate: [clientOnlyGuard],
        loadComponent: () => import('./components/client-profile/client-profile.component').then(m => m.ClientProfileComponent)
      },
      {
        path: 'about',
        loadComponent: () => import('./components/about/about.component').then(m => m.AboutComponent)
      },
      {
    path: 'reviews',
    loadComponent: () => import('./components/reviews/reviews.component').then(m => m.ReviewsComponent)
    },
      {
        path: 'profile-freelancer',
        canActivate: [freelancerOnlyGuard],
        loadComponent: () => import('./components/freelancer-profile/freelancer-profile.component').then(m => m.FreelancerProfileComponent)
      },
      {
        path: 'milestones-client',
        canActivate: [clientOnlyGuard],
        loadComponent: () => import('./components/milestone-client/milestone-client.component').then(m => m.MilestoneClientComponent)
      },
      {
        path: 'milestones-freelancer',
        canActivate: [freelancerOnlyGuard],
        loadComponent: () => import('./components/milestone-freelancer/milestone-freelancer.component').then(m => m.MilestoneFreelancerComponent)
      },
      {
        path: '**',
        redirectTo: ''
      }
    ]
  }
];
