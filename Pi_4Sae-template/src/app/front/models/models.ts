// User and Profile Models (UI-side, decoded from JWT + backend lookup)
export interface User {
  id: string;          // Keycloak sub (UUID)
  backendId?: number;  // Numeric ID from .NET user microservice
  /** true si l’utilisateur Keycloak n’a pas de ligne correspondante dans la base .NET */
  profileIncomplete?: boolean;
  email: string;
  firstName: string;
  lastName: string;
  userType: 'freelancer' | 'client';
  profileImage?: string;
  createdAt: Date;
}

// Backend User (.NET microservice)
export type BackendRole = 'FREELANCER' | 'CLIENT' | 'ADMIN';

export interface BackendUser {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  fullName: string;
  role: number;           // 0=FREELANCER, 1=CLIENT, 2=ADMIN
  profilePicture?: string | null;
  bio?: string | null;
  phoneNumber?: string | null;
  skills?: string | null;
  portfolioUrl?: string | null;
  companyName?: string | null;
  isVerified: boolean;
  isActive: boolean;
  createdAt: string;
  updatedAt?: string | null;
  keycloakId?: string | null;
}

export interface UserCreateRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  role: number;           // 0=FREELANCER, 1=CLIENT, 2=ADMIN
  profilePicture?: string;
  bio?: string;
  phoneNumber?: string;
  skills?: string;
  portfolioUrl?: string;
  companyName?: string;
}

export interface UserUpdateRequest {
  firstName?: string;
  lastName?: string;
  profilePicture?: string;
  bio?: string;
  phoneNumber?: string;
  skills?: string;
  portfolioUrl?: string;
  companyName?: string;
  isActive?: boolean;
  isVerified?: boolean;
}

export interface FreelancerProfile {
  userId: string;
  title: string;
  hourlyRate: number;
  skills: string[];
  bio: string;
  portfolio: PortfolioItem[];
  rating: number;
  totalJobs: number;
  totalEarnings: number;
  availability: 'available' | 'busy' | 'unavailable';
  languages: string[];
  location: string;
}

export interface ClientProfile {
  userId: string;
  companyName: string;
  industry: string;
  location: string;
  totalSpent: number;
  jobsPosted: number;
  rating: number;
}

export interface PortfolioItem {
  id: string;
  title: string;
  description: string;
  imageUrl: string;
  projectUrl?: string;
  tags: string[];
}

// =========================
// Project (Backend-aligned) Models
// =========================

export enum Experience {
  ENTRY = 'ENTRY',
  INTERMEDIATE = 'INTERMEDIATE',
  EXPERT = 'EXPERT'
}

export enum Status {
  OPEN = 'OPEN',
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED',
  ARCHIVED = 'ARCHIVED'
}

export interface ProjectDto {
  id: number;
  client_id: number;
  title: string;
  description: string;
  category: string;
  skills: string;
  budget_min: number;
  budget_max: number;
  duration: string;
  experienceLevel: Experience;
  status: Status;
  deadline: Date;
}

// ← clientEmail ajouté
export interface Project {
  id: number;
  clientId: number;
  clientEmail?: string;
  title: string;
  description: string;
  category: string;
  skills: string[];
  budgetMin: number;
  budgetMax: number;
  duration: string;
  experienceLevel: Experience;
  status: Status;
  deadline: Date;
  proposalsCount?: number;
}

export type ProposalStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED' | 'WITHDRAWN' | 'EXPIRED';

/** Freelancer Preferences - freelancer-level settings (skills, budget, etc.), NOT project-based */
export interface FreelancerPreferences {
  id?: number;
  /** Sujet Keycloak (string) renvoyé par le microservice candidature */
  freelancerId: string;
  skills: string[];
  preferredProjectTypes: string[];
  minBudget: number;
  maxBudget: number;
  avgDeliveryDays: number;
  openToNegotiation: boolean;
  openToOtherProjectTypes: boolean;
  notes?: string;
}

export interface ProposalApi {
  id?: number;
  projectId: number;
  freelancerId: number;
  proposedPrice: number;
  deliveryDays: number;
  coverLetter: string;
  status: string;
  isInvited: boolean;
  revisionsOffered: number;
}

export interface ProjectProposal {
  id: number;
  projectId: number;
  freelancerId: number;
  coverLetter: string;
  proposedBudget: number;
  estimatedDuration: string;
  status: ProposalStatus;
  submittedAt: Date;
  expiresAt?: Date;
  hiddenByClient?: boolean;
  deliveryDays?: number;
  isInvited?: boolean;
  revisionsOffered?: number;
}

export interface ProjectFilters {
  category?: string;
  status?: Status;
  budgetMin?: number;
  budgetMax?: number;
  experienceLevel?: Experience;
  query?: string;
  clientId?: number;
}

export interface FreelancerProposalStats {
  freelancerId: number;
  totalProposals: number;
  acceptedProposals: number;
  acceptanceRate: number;
}

// Job Models
export interface Job {
  id: string;
  clientId: string;
  title: string;
  description: string;
  category: string;
  skills: string[];
  budget: {
    type: 'fixed' | 'hourly';
    amount?: number;
    hourlyRate?: { min: number; max: number };
  };
  duration: string;
  experienceLevel: 'entry' | 'intermediate' | 'expert';
  status: 'open' | 'in-progress' | 'completed' | 'cancelled';
  postedAt: Date;
  proposals: number;
  clientInfo: {
    name: string;
    rating: number;
    totalSpent: number;
    location: string;
  };
}

export interface Proposal {
  id: string;
  jobId: string;
  freelancerId: string;
  coverLetter: string;
  proposedRate: number;
  estimatedDuration: string;
  status: 'pending' | 'accepted' | 'rejected' | 'withdrawn';
  submittedAt: Date;
  freelancerInfo: {
    name: string;
    title: string;
    rating: number;
    totalJobs: number;
    profileImage?: string;
  };
}

export interface Conversation {
  id: string;
  participants: string[];
  lastMessage: Message;
  unreadCount: number;
}

export interface Message {
  id: string;
  conversationId: string;
  senderId: string;
  content: string;
  timestamp: Date;
  read: boolean;
  attachments?: Attachment[];
}

export interface Attachment {
  id: string;
  name: string;
  url: string;
  type: string;
  size: number;
}

export interface Contract {
  id: string;
  jobId: string;
  freelancerId: string;
  clientId: string;
  status: 'active' | 'completed' | 'cancelled';
  terms: {
    rate: number;
    paymentType: 'fixed' | 'hourly';
    totalAmount?: number;
  };
  milestones?: Milestone[];
  startDate: Date;
  endDate?: Date;
  totalPaid: number;
}

export interface Milestone {
  id: string;
  description: string;
  amount: number;
  dueDate: Date;
  status: 'pending' | 'in-progress' | 'submitted' | 'approved' | 'paid';
}

export interface Review {
  id: string;
  contractId: string;
  reviewerId: string;
  revieweeId: string;
  rating: number;
  comment: string;
  createdAt: Date;
}

export interface JobFilters {
  category?: string;
  skills?: string[];
  budgetMin?: number;
  budgetMax?: number;
  experienceLevel?: string[];
  jobType?: string[];
  location?: string;
}

export interface SearchResult<T> {
  items: T[];
  total: number;
  page: number;
  pageSize: number;
}

// =========================
// Stats Models
// =========================

export interface PopularProject {
  id: number;
  title: string;
  category: string;
  budgetMin: number;
  budgetMax: number;
  status: string;
  proposalsCount: number;
}

// Forum Models (backend-aligned)
export interface ForumReply {
  id?: number;
  author: string;
  content: string;
  createdAt?: string;
}

export interface ForumPost {
  id?: number;
  author: string;
  title: string;
  content: string;
  imageUrl?: string | null;
  audioUrl?: string | null;
  videoUrl?: string | null;
  createdAt?: string;
  reactions?: string;       // JSON string e.g. '{"👍":3,"❤️":1}'
  replies?: ForumReply[];
}

export interface ProjectStatsDTO {
  totalProjects: number;
  openProjects: number;
  inProgressProjects: number;
  completedProjects: number;
  cancelledProjects: number;
  archivedProjects: number;
  averageBudgetMin: number;
  averageBudgetMax: number;
  averageBudget: number;
  totalBudgetMax: number;
  mostPopularProjects: PopularProject[];
  topCategories: string[];
  topSkills: string[];
}