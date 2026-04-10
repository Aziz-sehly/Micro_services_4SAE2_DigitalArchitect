package com.esprit.microservice_project.Services;

import com.esprit.microservice_project.DTO.ProjectStatsDTO;
import com.esprit.microservice_project.Entity.Experience;
import com.esprit.microservice_project.Entity.Project;
import com.esprit.microservice_project.Entity.Status;
import com.esprit.microservice_project.Repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceProject implements IServiceProject {

    @Autowired
    private ProjectRepository projectRepository;

    private String blank(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    // ✅ CREATE
    @Override
    public Project addProject(Project project) {
        if (project.getStatus() == null) {
            project.setStatus(Status.OPEN);
        }
        return projectRepository.save(project);
    }

    // ✅ UPDATE
    @Override
    public Project updateProject(int id, Project newProject) {

        Project existingProject = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        existingProject.setTitle(newProject.getTitle());
        existingProject.setDescription(newProject.getDescription());
        existingProject.setCategory(newProject.getCategory());
        existingProject.setSkills(newProject.getSkills());
        existingProject.setBudget_min(newProject.getBudget_min());
        existingProject.setBudget_max(newProject.getBudget_max());
        existingProject.setDuration(newProject.getDuration());
        existingProject.setExperienceLevel(newProject.getExperienceLevel());
        existingProject.setStatus(newProject.getStatus());
        existingProject.setDeadline(newProject.getDeadline());

        // ✅ ONLY EMAIL can be updated
        if (newProject.getClientEmail() != null) {
            existingProject.setClientEmail(newProject.getClientEmail());
        }

        // ❌ DO NOT TOUCH clientId (comes from JWT)

        return projectRepository.save(existingProject);
    }

    // ✅ GET ALL
    @Override
    public List<Project> getProjects() {
        return projectRepository.findAll();
    }

    // ✅ GET ONE
    @Override
    public Project getProject(int id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
    }

    // ✅ DELETE
    @Override
    public void deleteProject(int id) {
        if (projectRepository.existsById(id)) {
            projectRepository.deleteById(id);
        }
    }

    // ✅ GET PROJECTS BY KEYCLOAK USER
    @Override
    public List<Project> getProjectsByClientId(String clientId) {
        return projectRepository.findByClientId(clientId);
    }

    // ✅ SEARCH
    @Override
    public List<Project> search(String query, String category,
                                Status status, Experience experience,
                                Float budgetMin, Float budgetMax) {

        return projectRepository.search(
                blank(query),
                blank(category),
                status,
                experience,
                budgetMin,
                budgetMax
        );
    }

    // ✅ FILTER
    @Override
    public List<Project> filter(String category, Status status,
                                Experience experience,
                                Float budgetMin, Float budgetMax) {

        return projectRepository.filter(
                blank(category),
                status,
                experience,
                budgetMin,
                budgetMax
        );
    }

    // ✅ CLIENT STATS (filtered by user)
    @Override
    public ProjectStatsDTO getClientStats(String clientId) {
        List<Project> projects = projectRepository.findByClientId(clientId);
        return buildStats(projects);
    }

    // ✅ FREELANCER STATS (all projects)
    @Override
    public ProjectStatsDTO getFreelancerStats() {
        return buildStats(projectRepository.findAll());
    }

    // ✅ COMMON STATS BUILDER
    private ProjectStatsDTO buildStats(List<Project> projects) {

        ProjectStatsDTO dto = new ProjectStatsDTO();

        dto.setTotalProjects(projects.size());
        dto.setOpenProjects(projects.stream().filter(p -> p.getStatus() == Status.OPEN).count());
        dto.setInProgressProjects(projects.stream().filter(p -> p.getStatus() == Status.IN_PROGRESS).count());
        dto.setCompletedProjects(projects.stream().filter(p -> p.getStatus() == Status.COMPLETED).count());
        dto.setCancelledProjects(projects.stream().filter(p -> p.getStatus() == Status.CANCELLED).count());
        dto.setArchivedProjects(projects.stream().filter(p -> p.getStatus() == Status.DRAFT).count());

        double avgMin = projects.stream()
                .mapToDouble(p -> p.getBudget_min() != null ? p.getBudget_min() : 0)
                .average().orElse(0);

        double avgMax = projects.stream()
                .mapToDouble(p -> p.getBudget_max() != null ? p.getBudget_max() : 0)
                .average().orElse(0);

        dto.setAverageBudgetMin(avgMin);
        dto.setAverageBudgetMax(avgMax);
        dto.setAverageBudget((avgMin + avgMax) / 2);

        dto.setTotalBudgetMax(projects.stream()
                .mapToDouble(p -> p.getBudget_max() != null ? p.getBudget_max() : 0)
                .sum());

        return dto;
    }

    // ✅ GET BY ID (used for email notification)
    @Override
    public Project getProjectById(int id) {
        return projectRepository.findById(id).orElse(null);
    }
}
