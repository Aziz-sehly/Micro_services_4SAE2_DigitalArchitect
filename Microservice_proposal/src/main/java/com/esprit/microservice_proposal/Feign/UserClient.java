package com.esprit.microservice_proposal.Feign;

import com.esprit.microservice_proposal.DTO.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "microservice-user",
        url = "${proposal.feign.user-url:http://localhost:5088/api}"
)
public interface UserClient {

    @GetMapping("/Users/{id}")
    User getUserById(@PathVariable("id") long id);
}