package com.esprit.microservice_proposal.Controller;

import com.esprit.microservice_proposal.DTO.Project;
import com.esprit.microservice_proposal.DTO.User;
import com.esprit.microservice_proposal.Entity.Proposal;
import com.esprit.microservice_proposal.Services.IServiceProposal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/proposal")
public class ProposalRest {

    @Autowired
    IServiceProposal serviceProposal;

    // FREELANCER: Submit proposal
    @PostMapping("/AddProposal")
    @PreAuthorize("hasRole('freelancer')")
    public Proposal AddProposal(@RequestBody Proposal p, @AuthenticationPrincipal Jwt jwt) {
        // Get current freelancer ID from token
        String freelancerId = jwt.getSubject();
        // Optionally set it: p.setFreelancerId(freelancerId);
        return serviceProposal.addProposal(p);
    }

    // BOTH: View all proposals (admin/moderation)
    @GetMapping("/GetAllProposals")
    @PreAuthorize("hasAnyRole('client', 'freelancer')")
    public List<Proposal> GetAllProposals() {
        return serviceProposal.getProposals();
    }

    // BOTH: View single proposal
    @GetMapping("/GetProposal/{id}")
    @PreAuthorize("hasAnyRole('client', 'freelancer')")
    public Proposal GetProposal(@PathVariable int id) {
        return serviceProposal.getProposal(id);
    }

    // FREELANCER: Update own proposal
    @PutMapping("/UpdateProposal/{id}")
    @PreAuthorize("hasRole('freelancer')")
    public Proposal UpdateProposal(@PathVariable int id, @RequestBody Proposal p, @AuthenticationPrincipal Jwt jwt) {
        // Verify ownership in service layer
        return serviceProposal.updateProposal(id, p);
    }

    // FREELANCER: Delete own proposal
    @DeleteMapping("/DeleteProposal/{id}")
    @PreAuthorize("hasRole('freelancer')")
    public void DeleteProposal(@PathVariable int id, @AuthenticationPrincipal Jwt jwt) {
        serviceProposal.deleteProposal(id);
    }

    // CLIENT: View proposals for their project
    @GetMapping("/GetProposalsByProject/{projectId}")
    @PreAuthorize("hasRole('client')")
    public List<Proposal> GetProposalsByProject(@PathVariable int projectId, @AuthenticationPrincipal Jwt jwt) {
        // Verify project ownership in service layer using jwt.getSubject()
        return serviceProposal.getProposalsByProjectId(projectId);
    }

    // FREELANCER: View their own proposals
    @GetMapping("/GetProposalsByFreelancer/{freelancerId}")
    @PreAuthorize("hasRole('freelancer')")
    public List<Proposal> GetProposalsByFreelancer(@PathVariable int freelancerId, @AuthenticationPrincipal Jwt jwt) {
        // Verify the freelancerId matches jwt.getSubject()
        return serviceProposal.getProposalsByFreelancerId(freelancerId);
    }

    // BOTH: Feign — get Project (view only)
    @GetMapping("/GetProject/{id}")
    @PreAuthorize("hasAnyRole('client', 'freelancer')")
    public Project GetProject(@PathVariable int id) {
        return serviceProposal.getProjectById(id);
    }

    // BOTH: Feign — get Freelancer (view only)
    @GetMapping("/GetFreelancer/{id}")
    @PreAuthorize("hasAnyRole('client', 'freelancer')")
    public User GetFreelancer(@PathVariable int id) {
        return serviceProposal.getFreelancerById(id);
    }

    // BOTH: Feign — get all Projects
    @GetMapping("/GetAllProjects")
    @PreAuthorize("hasAnyRole('client', 'freelancer')")
    public List<Project> GetAllProjects() {
        return serviceProposal.getAllProjects();
    }

    // CLIENT: Accept proposal (new endpoint)
    @PutMapping("/AcceptProposal/{id}")
    @PreAuthorize("hasRole('client')")
    public ResponseEntity<Proposal> AcceptProposal(@PathVariable int id, @AuthenticationPrincipal Jwt jwt) {
        Proposal accepted = serviceProposal.acceptProposal(id, jwt.getSubject());
        return ResponseEntity.ok(accepted);
    }

    // FREELANCER: Withdraw proposal (new endpoint)
    @PutMapping("/WithdrawProposal/{id}")
    @PreAuthorize("hasRole('freelancer')")
    public ResponseEntity<Proposal> WithdrawProposal(@PathVariable int id, @AuthenticationPrincipal Jwt jwt) {
        Proposal withdrawn = serviceProposal.withdrawProposal(id, jwt.getSubject());
        return ResponseEntity.ok(withdrawn);
    }
}