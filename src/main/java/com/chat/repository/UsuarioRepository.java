package com.chat.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.chat.entity.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    @Query("SELECT usuario.id FROM Usuario usuario WHERE usuario.email = :email")
    Optional<Long> findIdByEmail(@Param("email") String email);
    
    boolean existsByEmail(String email);

    List<Usuario> findByNameContainingIgnoreCase(String nome);
}
