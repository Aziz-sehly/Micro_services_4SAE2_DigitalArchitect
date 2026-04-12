package com.esprit.microservice_project.Contoller;

import com.esprit.microservice_project.DTO.ProjectAISuggestRequest;
import com.esprit.microservice_project.DTO.ProjectAISuggestResponse;
import com.esprit.microservice_project.DTO.ProjectStatsDTO;
import com.esprit.microservice_project.DTO.ProposalNotificationDTO;
import com.esprit.microservice_project.Entity.Experience;
import com.esprit.microservice_project.Entity.Project;
import com.esprit.microservice_project.Entity.Status;
import com.esprit.microservice_project.Services.AIProjectSuggestService;
import com.esprit.microservice_project.Services.IServiceProject;
import com.esprit.microservice_project.Services.EmailService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/project")
public class ProjectRest {

    @Autowired
    IServiceProject serviceproject;

    @Autowired
    AIProjectSuggestService aiSuggestService;

    @Autowired
    EmailService emailService;

    // ── AI suggest ─────────────────────────────────────────────
    @PostMapping("/ai-suggest")
    @PreAuthorize("hasRole('client')")
    public ProjectAISuggestResponse aiSuggest(@RequestBody ProjectAISuggestRequest req) {
        return aiSuggestService.suggest(req.getDescription(), req.getDuration());
    }

    // ── CREATE PROJECT ─────────────────────────────────────────
    @PostMapping("/add")
    @PreAuthorize("hasRole('client')")
    public Project addProject(@RequestBody Project p, @AuthenticationPrincipal Jwt jwt) {

        // ✅ Link project to Keycloak user
        p.setClientId(jwt.getSubject());
        p.setClientEmail(jwt.getClaim("email"));

        boolean needsAI = p.getDescription() != null && !p.getDescription().isBlank()
                && (isBlank(p.getTitle()) || isBlank(p.getSkills())
                || p.getBudget_min() == null || p.getBudget_max() == null);

        if (needsAI) {
            String duration = p.getDuration() != null ? p.getDuration() : "";

            ProjectAISuggestResponse suggestion =
                    aiSuggestService.suggest(p.getDescription(), duration);

            if (isBlank(p.getTitle()))     p.setTitle(suggestion.getTitle());
            if (isBlank(p.getSkills()))    p.setSkills(suggestion.getSkills());
            if (p.getBudget_min() == null) p.setBudget_min(suggestion.getBudgetMin());
            if (p.getBudget_max() == null) p.setBudget_max(suggestion.getBudgetMax());
        }

        return serviceproject.addProject(p);
    }

    // ── GET ALL PROJECTS ───────────────────────────────────────
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('client', 'freelancer')")
    public List<Project> getAllProjects() {
        return serviceproject.getProjects();
    }

    // ── GET ONE PROJECT ───────────────────────────────────────
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('client', 'freelancer')")
    public Project getProject(@PathVariable int id) {
        return serviceproject.getProject(id);
    }

    // ── UPDATE PROJECT (OWNER ONLY) ────────────────────────────
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('client')")
    public Project updateProject(@PathVariable int id,
                                 @RequestBody Project p,
                                 @AuthenticationPrincipal Jwt jwt) {

        Project existing = serviceproject.getProject(id);

        // ✅ Ownership check
        if (!existing.getClientId().equals(jwt.getSubject())) {
            throw new RuntimeException("Unauthorized: Not your project");
        }

        return serviceproject.updateProject(id, p);
    }

    // ── DELETE PROJECT (OWNER ONLY) ────────────────────────────
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('client')")
    public ResponseEntity<String> deleteProject(@PathVariable int id,
                                                @AuthenticationPrincipal Jwt jwt) {

        Project existing = serviceproject.getProject(id);

        // ✅ Ownership check
        if (!existing.getClientId().equals(jwt.getSubject())) {
            return ResponseEntity.status(403).body("Unauthorized");
        }

        serviceproject.deleteProject(id);
        return ResponseEntity.ok("Project deleted");
    }

    // ── MY PROJECTS (from JWT) ─────────────────────────────────
    @GetMapping("/my-projects")
    @PreAuthorize("hasRole('client')")
    public List<Project> getMyProjects(@AuthenticationPrincipal Jwt jwt) {
        String clientId = jwt.getSubject();
        return serviceproject.getProjectsByClientId(clientId);
    }

    // ── SEARCH ────────────────────────────────────────────────
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('client', 'freelancer')")
    public List<Project> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) Experience experience,
            @RequestParam(required = false) Float budgetMin,
            @RequestParam(required = false) Float budgetMax) {

        return serviceproject.search(query, category, status, experience, budgetMin, budgetMax);
    }

    // ── FILTER ────────────────────────────────────────────────
    @GetMapping("/filter")
    @PreAuthorize("hasAnyRole('client', 'freelancer')")
    public List<Project> filter(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) Experience experience,
            @RequestParam(required = false) Float budgetMin,
            @RequestParam(required = false) Float budgetMax) {

        return serviceproject.filter(category, status, experience, budgetMin, budgetMax);
    }

    // ── CLIENT STATS ──────────────────────────────────────────
    @GetMapping("/stats")
    @PreAuthorize("hasRole('client')")
    public ProjectStatsDTO getStats(@AuthenticationPrincipal Jwt jwt) {
        String clientId = jwt.getSubject();
        return serviceproject.getClientStats(clientId);
    }

    // ── FREELANCER STATS ──────────────────────────────────────
    @GetMapping("/stats/freelancer")
    @PreAuthorize("hasRole('freelancer')")
    public ProjectStatsDTO getFreelancerStats() {
        return serviceproject.getFreelancerStats();
    }

    // ── EMAIL NOTIFICATION ────────────────────────────────────
    @PostMapping("/notify/new-proposal")
    @PreAuthorize("hasAnyRole('client', 'freelancer')")
    public ResponseEntity<String> notifyNewProposal(@RequestBody ProposalNotificationDTO dto) {

        Project project = serviceproject.getProjectById(dto.getProjectId());

        if (project == null)
            return ResponseEntity.notFound().build();

        String clientEmail = project.getClientEmail();

        if (clientEmail == null || clientEmail.isBlank())
            return ResponseEntity.badRequest().body("No client email");

        emailService.sendNewProposalNotification(
                clientEmail,
                project.getTitle(),
                project.getCategory(),
                dto.getProposedBudget(),
                dto.getDeliveryDays(),
                dto.getCoverLetter()
        );

        return ResponseEntity.ok("Email sent to " + clientEmail);
    }

    // ── UTIL ──────────────────────────────────────────────────
    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
