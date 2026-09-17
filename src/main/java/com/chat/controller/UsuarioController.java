package com.chat.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.chat.dto.UsuarioResumoDTO;
import com.chat.service.UsuarioService;

@RestController
@RequestMapping("/usuario")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/buscar-id-usuario/{email}")
    public Long buscarIdPorEmail(@PathVariable String email) {
        validarTexto(email, "O e-mail não pode estar vazio.");

        return usuarioService.buscarIdPorEmail(email.trim())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuário não encontrado."));
    }

    @GetMapping("/buscar-usuarios/{nome}")
    public List<UsuarioResumoDTO> buscarUsuarios(@PathVariable String nome) {
        validarTexto(nome, "O nome não pode estar vazio.");

        List<UsuarioResumoDTO> usuarios = usuarioService.buscarPorNome(nome.trim())
                .stream()
                .map(UsuarioResumoDTO::from)
                .toList();

        if (usuarios.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Usuário não encontrado.");
        }

        return usuarios;
    }

    private void validarTexto(String valor, String mensagem) {
        if (valor == null || valor.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
        }
    }
}
