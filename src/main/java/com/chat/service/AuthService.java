package com.chat.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.chat.dto.UsuarioDTO;
import com.chat.entity.Usuario;
import com.chat.enumeration.Role;
import com.chat.repository.UsuarioRepository;

@Service 
public class AuthService {

    @Autowired 
    private UsuarioRepository usuarioRepository;

    @Autowired 
    private PasswordEncoder passwordEncoder;


    public void cadastrarUsuario(UsuarioDTO usuarioDTO) {

        if(usuarioRepository.existsByEmail(usuarioDTO.email())){
            throw new IllegalArgumentException("Email já cadastrado");
        }

        Usuario user = new Usuario();

        user.setName(usuarioDTO.nome());
        user.setEmail(usuarioDTO.email());
        user.setPassword(passwordEncoder.encode(usuarioDTO.senha()));
        user.setRole(Role.USER);

        System.out.println("Usuario salvo: " + user.getName() + ", Email: " + user.getEmail() + ", Role: " + user.getRole());
        usuarioRepository.save(user);

    }
}
