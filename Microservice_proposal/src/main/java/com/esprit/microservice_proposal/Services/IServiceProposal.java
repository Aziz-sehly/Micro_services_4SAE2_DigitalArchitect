package com.esprit.microservice_proposal.Services;

import com.esprit.microservice_proposal.DTO.Project;
import com.esprit.microservice_proposal.DTO.User;
import com.esprit.microservice_proposal.Entity.Proposal;
import java.util.List;

public interface IServiceProposal {
    Proposal addProposal(Proposal proposal);
    Proposal updateProposal(int id, Proposal newProposal);
    List<Proposal> getProposals();
    Proposal getProposal(int id);
    void deleteProposal(int id);
    List<Proposal> getProposalsByProjectId(int projectId);
    List<Proposal> getProposalsByFreelancerId(int freelancerId);
    List<Proposal> getProposalsByKeycloakId(String keycloakId);
    List<Proposal> getProposalsByFreelancerIdVerified(int freelancerId, String keycloakSub);
    Proposal acceptProposal(int proposalId, String clientId);
    Proposal rejectProposal(int proposalId, String clientId);
    Proposal withdrawProposal(int proposalId, String freelancerId);
    // Méthodes Feign — comme dans le cours
    Project getProjectById(int id);
    User getFreelancerById(long id);
    List<Project> getAllProjects();
}