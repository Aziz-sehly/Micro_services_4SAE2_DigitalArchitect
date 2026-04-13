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
    private IServiceProposal serviceProposal;

    @PostMapping("/AddProposal")
    @PreAuthorize("hasRole('freelancer')")
    public ResponseEntity<Proposal> AddProposal(@RequestBody Proposal p, @AuthenticationPrincipal Jwt jwt) {
        p.setFreelancerKeycloakId(jwt.getSubject());
        return ResponseEntity.ok(serviceProposal.addProposal(p));
    }

    @GetMapping("/GetAllProposals")
    @PreAuthorize("hasAnyRole('client', 'freelancer')")
    public List<Proposal> GetAllProposals() {
        return serviceProposal.getProposals();
    }

    @GetMapping("/GetProposal/{id}")
    @PreAuthorize("hasAnyRole('client', 'freelancer')")
    public ResponseEntity<Proposal> GetProposal(@PathVariable int id) {
        return ResponseEntity.ok(serviceProposal.getProposal(id));
    }

    @PutMapping("/UpdateProposal/{id}")
    @PreAuthorize("hasRole('freelancer')")
    public ResponseEntity<Proposal> UpdateProposal(@PathVariable int id,
                                                   @RequestBody Proposal p,
                                                   @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(serviceProposal.updateProposal(id, p));
    }

    @DeleteMapping("/DeleteProposal/{id}")
    @PreAuthorize("hasRole('freelancer')")
    public ResponseEntity<Void> DeleteProposal(@PathVariable int id, @AuthenticationPrincipal Jwt jwt) {
        serviceProposal.deleteProposal(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/GetProposalsByProject/{projectId}")
    @PreAuthorize("hasRole('client')")
    public List<Proposal> GetProposalsByProject(@PathVariable int projectId, @AuthenticationPrincipal Jwt jwt) {
        return serviceProposal.getProposalsByProjectId(projectId);
    }

    @GetMapping("/GetProposalsByFreelancer/{freelancerId}")
    @PreAuthorize("hasRole('freelancer')")
    public List<Proposal> GetProposalsByFreelancer(@PathVariable int freelancerId,
                                                   @AuthenticationPrincipal Jwt jwt) {
        return serviceProposal.getProposalsByFreelancerIdVerified(freelancerId, jwt.getSubject());
    }

    @GetMapping("/GetMyProposals")
    @PreAuthorize("hasRole('freelancer')")
    public List<Proposal> GetMyProposals(@AuthenticationPrincipal Jwt jwt) {
        return serviceProposal.getProposalsByKeycloakId(jwt.getSubject());
    }

    @GetMapping("/GetProject/{id}")
    @PreAuthorize("hasAnyRole('client', 'freelancer')")
    public Project GetProject(@PathVariable int id) {
        return serviceProposal.getProjectById(id);
    }

    @GetMapping("/GetFreelancer/{id}")
    @PreAuthorize("hasAnyRole('client', 'freelancer')")
    public User GetFreelancer(@PathVariable long id) {
        return serviceProposal.getFreelancerById(id);
    }

    @GetMapping("/GetAllProjects")
    @PreAuthorize("hasAnyRole('client', 'freelancer')")
    public List<Project> GetAllProjects() {
        return serviceProposal.getAllProjects();
    }

    @PutMapping("/AcceptProposal/{id}")
    @PreAuthorize("hasRole('client')")
    public ResponseEntity<Proposal> AcceptProposal(@PathVariable int id, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(serviceProposal.acceptProposal(id, jwt.getSubject()));
    }

    @PutMapping("/RejectProposal/{id}")
    @PreAuthorize("hasRole('client')")
    public ResponseEntity<Proposal> RejectProposal(@PathVariable int id, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(serviceProposal.rejectProposal(id, jwt.getSubject()));
    }

    @PutMapping("/WithdrawProposal/{id}")
    @PreAuthorize("hasRole('freelancer')")
    public ResponseEntity<Proposal> WithdrawProposal(@PathVariable int id, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(serviceProposal.withdrawProposal(id, jwt.getSubject()));
    }
}
