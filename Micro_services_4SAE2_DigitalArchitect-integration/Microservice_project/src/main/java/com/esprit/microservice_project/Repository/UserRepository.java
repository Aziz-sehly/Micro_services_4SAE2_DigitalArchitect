package com.esprit.microservice_project.Repository;

import com.esprit.microservice_project.DTO.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
}
