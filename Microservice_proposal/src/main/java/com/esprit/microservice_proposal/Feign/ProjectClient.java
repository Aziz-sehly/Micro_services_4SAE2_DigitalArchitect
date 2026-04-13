package com.esprit.microservice_proposal.Feign;

import com.esprit.microservice_proposal.DTO.Project;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@FeignClient(
        name = "microservice-project",
        url = "${proposal.feign.project-url:http://localhost:8091/freelance}"
)
public interface ProjectClient {

    @GetMapping("/project/all")
    List<Project> getAllProjects();

    @GetMapping("/project/{id}")
    Project getProjectById(@PathVariable("id") int id);

    /** PUT : Feign (HttpURLConnection) ne supporte pas PATCH de façon fiable. */
    @PutMapping("/project/{id}/status")
    Project patchProjectStatus(@PathVariable("id") int id, @RequestBody Map<String, String> body);
}