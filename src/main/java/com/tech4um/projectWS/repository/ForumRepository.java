package com.tech4um.projectWS.repository;

import com.tech4um.projectWS.model.Forum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository; // Adicionado import

import java.util.Optional;

@Repository
public interface ForumRepository extends JpaRepository<Forum,Long> {

    /*
     * Método customizado para checar se o TÍTULO já existe.
     * Necessário para a lógica de unicidade no ForumService.
     */
    Optional<Forum> findByTitle(String title);
}