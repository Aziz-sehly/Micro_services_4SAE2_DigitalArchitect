package com.esprit.microservice_proposal.Services;

import com.esprit.microservice_proposal.DTO.Project;
import com.esprit.microservice_proposal.DTO.User;
import com.esprit.microservice_proposal.Entity.Proposal;
import com.esprit.microservice_proposal.Entity.ProposalStatus;
import com.esprit.microservice_proposal.Feign.ProjectClient;
import com.esprit.microservice_proposal.Feign.UserClient;
import com.esprit.microservice_proposal.Repository.ProposalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class ServiceProposal implements IServiceProposal {

    @Autowired
    private ProposalRepository proposalRepository;

    @Autowired
    private ProjectClient projectClient;

    @Autowired
    private UserClient userClient;

    @Override
    public Proposal addProposal(Proposal proposal) {
        proposal.setStatus(ProposalStatus.PENDING);
        return proposalRepository.save(proposal);
    }

    @Override
    public Proposal updateProposal(int id, Proposal newProposal) {
        Proposal existing = proposalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proposal not found: " + id));

        existing.setProjectId(newProposal.getProjectId());
        existing.setFreelancerId(newProposal.getFreelancerId());
        existing.setProposedPrice(newProposal.getProposedPrice());
        existing.setDeliveryDays(newProposal.getDeliveryDays());
        existing.setCoverLetter(newProposal.getCoverLetter());
        existing.setStatus(newProposal.getStatus());
        existing.setIsInvited(newProposal.getIsInvited());
        existing.setRevisionsOffered(newProposal.getRevisionsOffered());

        if (newProposal.getFreelancerKeycloakId() != null) {
            existing.setFreelancerKeycloakId(newProposal.getFreelancerKeycloakId());
        }

        return proposalRepository.save(existing);
    }

    @Override
    public List<Proposal> getProposals() {
        return proposalRepository.findAll();
    }

    @Override
    public Proposal getProposal(int id) {
        return proposalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proposal not found: " + id));
    }

    @Override
    public void deleteProposal(int id) {
        if (!proposalRepository.existsById(id)) {
            throw new RuntimeException("Proposal not found: " + id);
        }
        proposalRepository.deleteById(id);
    }

    @Override
    public List<Proposal> getProposalsByProjectId(int projectId) {
        return proposalRepository.findByProjectId(projectId);
    }

    @Override
    public List<Proposal> getProposalsByFreelancerId(int freelancerId) {
        return proposalRepository.findByFreelancerId(freelancerId);
    }

    @Override
    public List<Proposal> getProposalsByKeycloakId(String keycloakId) {
        return proposalRepository.findByFreelancerKeycloakId(keycloakId);
    }

    @Override
    public List<Proposal> getProposalsByFreelancerIdVerified(int freelancerId, String keycloakSub) {
        User u = userClient.getUserById(freelancerId);
        if (u == null || u.getKeycloakId() == null || !u.getKeycloakId().equals(keycloakSub)) {
            throw new RuntimeException("Unauthorized: freelancer id does not match token");
        }
        return proposalRepository.findByFreelancerId(freelancerId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Proposal acceptProposal(int proposalId, String clientKeycloakId) {
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new RuntimeException("Proposal not found: " + proposalId));
        if (proposal.getStatus() != ProposalStatus.PENDING) {
            throw new RuntimeException("Proposal is not pending. Status: " + proposal.getStatus());
        }

        Project project = projectClient.getProjectById(proposal.getProjectId());
        if (project == null) {
            throw new RuntimeException("Project not found: " + proposal.getProjectId());
        }
        if (project.getClientId() == null || project.getClientId().isBlank()) {
            throw new RuntimeException("Project has no clientId set — impossible de vérifier le propriétaire");
        }
        if (!clientKeycloakId.equals(project.getClientId())) {
            throw new RuntimeException(
                    "Unauthorized: ce projet appartient à un autre compte Keycloak. "
                            + "Connecte-toi avec le même compte client qui a créé le projet, "
                            + "ou recrée le projet avec ton utilisateur actuel.");
        }

        for (Proposal sibling : proposalRepository.findByProjectId(project.getId())) {
            if (!sibling.getId().equals(proposalId) && sibling.getStatus() == ProposalStatus.PENDING) {
                sibling.setStatus(ProposalStatus.REJECTED);
                proposalRepository.save(sibling);
            }
        }

        proposal.setStatus(ProposalStatus.ACCEPTED);
        proposal = proposalRepository.save(proposal);

        projectClient.patchProjectStatus(proposal.getProjectId(), Map.of("status", "IN_PROGRESS"));

        return proposal;
    }

    @Override
    public Proposal rejectProposal(int proposalId, String clientKeycloakId) {
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new RuntimeException("Proposal not found: " + proposalId));
        if (proposal.getStatus() != ProposalStatus.PENDING) {
            throw new RuntimeException("Proposal is not pending. Status: " + proposal.getStatus());
        }

        Project project = projectClient.getProjectById(proposal.getProjectId());
        if (project == null) {
            throw new RuntimeException("Project not found: " + proposal.getProjectId());
        }
        if (project.getClientId() == null || !clientKeycloakId.equals(project.getClientId())) {
            throw new RuntimeException("Unauthorized: not your project");
        }

        proposal.setStatus(ProposalStatus.REJECTED);
        return proposalRepository.save(proposal);
    }

    @Override
    public Proposal withdrawProposal(int proposalId, String freelancerKeycloakId) {
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new RuntimeException("Proposal not found: " + proposalId));
        if (proposal.getStatus() != ProposalStatus.PENDING) {
            throw new RuntimeException("Proposal is not pending. Status: " + proposal.getStatus());
        }
        if (proposal.getFreelancerKeycloakId() == null
                || !proposal.getFreelancerKeycloakId().equals(freelancerKeycloakId)) {
            throw new RuntimeException("Unauthorized: not your proposal");
        }
        proposal.setStatus(ProposalStatus.WITHDRAWN);
        return proposalRepository.save(proposal);
    }

    @Override
    public Project getProjectById(int id) {
        return projectClient.getProjectById(id);
    }

    @Override
    public User getFreelancerById(long id) {
        return userClient.getUserById(id);
    }

    @Override
    public List<Project> getAllProjects() {
        return projectClient.getAllProjects();
    }
}
