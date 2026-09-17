package com.chat.dto;

import com.chat.entity.Usuario;

public record UsuarioResumoDTO(
        Long id,
        String nome) {

    public static UsuarioResumoDTO from(Usuario usuario) {
        return new UsuarioResumoDTO(usuario.getId(), usuario.getName());
    }
}
