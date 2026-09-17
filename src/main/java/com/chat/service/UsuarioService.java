package com.chat.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.chat.entity.Usuario;
import com.chat.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Optional<Long> buscarIdPorEmail(String email) {
        return usuarioRepository.findIdByEmail(email);
    }

    public List<Usuario> buscarPorNome(String nome) {
        return usuarioRepository.findByNameContainingIgnoreCase(nome);
    }
}
