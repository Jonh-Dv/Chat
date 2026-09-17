package com.chat.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.chat.entity.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    @Query(value = "SELECT tu.id_user FROM chat.tb_usuario tu WHERE tu.email LIKE :email", nativeQuery = true)
    Optional<Long> findIdByEmail(@Param("email") String email);
    
    boolean existsByEmail(String email);

}
