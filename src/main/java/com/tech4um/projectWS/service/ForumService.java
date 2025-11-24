package com.tech4um.projectWS.service;

import com.tech4um.projectWS.exception.ConflictException;
import com.tech4um.projectWS.exception.ResourceNotFoundException;
import com.tech4um.projectWS.model.Forum;
import com.tech4um.projectWS.repository.ForumRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ForumService {

    //Logger para rastreamento
    private static final Logger logger = LoggerFactory.getLogger(ForumService.class);

    public final ForumRepository forumRepository;

    public ForumService(ForumRepository forumRepository){
        this.forumRepository = forumRepository;
    }

    //Lógica: Criar novo Fórum
    public Forum createForum(Forum forum){
        logger.info("Tentando criar novo fórum: {}", forum.getName());

        //Regra de Negócio: Fóruns devem ter nomes únicos
        // O método findByName DEVE existir na interface ForumRepository
        Optional<Forum> existingForum = forumRepository.findByName(forum.getName());
        if (existingForum.isPresent()){
            throw new ConflictException("Já existe um fórum com o nome: " + forum.getName());
        }
        return forumRepository.save(forum);
    }

    //Lógica: Listar todos
    public List<Forum> findAllForums(){
        return forumRepository.findAll();
    }

    //Lógica: Encontrar por ID
    // 💡 CORRIGIDO: O ID agora é Long
    public Forum findById(Long id){
        return forumRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fórum não encontrado para a exclusão com ID: " + id));
    }

    //Lógica: Excluir
    public void deleteForum(Long id){
        // O método existsById deve existir e aceitar Long
        if (!forumRepository.existsById(id)){
            throw new ResourceNotFoundException("Fórum não encontrado para exclusão com ID: " + id);
        }
        forumRepository.deleteById(id);
    }
}