package com.tech4um.projectWS.repository;

import com.tech4um.projectWS.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/*
 * Repositório JPA para a entidade {@link User}.
 * Oferece métodos CRUD e consultas personalizadas para interagir com a tabela 'users'.
 * O tipo da chave primária (ID) é Long.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /*
     * Busca um usuário pelo endereço de e-mail.
     * Necessário para o processo de login e autenticação (Spring Security).
     * @param email O endereço de e-mail a ser pesquisado.
     * @return Um {@code Optional} contendo o usuário, se encontrado.
     */
    Optional<User> findByEmail(String email);

    /*
     * Busca um usuário pelo nome de usuário.
     * Pode ser utilizado em validações ou para buscar o usuário através do nome.
     * @param username O nome de usuário a ser pesquisado.
     * @return Um {@code Optional} contendo o usuário, se encontrado.
     */
    Optional<User> findByUsername(String username);

    /*
     * Busca um usuário pelo token de redefinição de senha.
     * Essencial para a funcionalidade "Esqueci Minha Senha".
     * @param token O token de redefinição de senha.
     * @return Um {@code Optional} contendo o usuário associado a este token.
     */
    Optional<User> findByResetPasswordToken(String token);
}