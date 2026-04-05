package com.esprit.microservice_project.Config;

import com.esprit.microservice_project.DTO.User;
import com.esprit.microservice_project.Repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataLoader {

    /**
     * Crée un utilisateur client par défaut (id=1) au démarrage si la table est vide.
     * Nécessaire pour que l'ajout de projets fonctionne (Project.client obligatoire).
     */
    @Bean
    CommandLineRunner initDefaultUser(UserRepository userRepository) {
        return args -> {
            if (userRepository.count() == 0) {
                User defaultClient = new User();
                defaultClient.setName("Client par défaut");
                defaultClient.setEmail("client@default.com");
                defaultClient.setRole("CLIENT");
                userRepository.save(defaultClient);
            }
        };
    }
}
