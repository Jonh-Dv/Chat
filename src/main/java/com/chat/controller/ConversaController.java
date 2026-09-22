package com.chat.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.chat.dto.ConversaDTO;
import com.chat.entity.Conversa;
import com.chat.service.ConversaService;
import com.chat.service.UsuarioService;

@RestController
@RequestMapping("/conversa")
public class ConversaController {

    private final ConversaService conversaService;
    private final UsuarioService usuarioService;

    public ConversaController(
            ConversaService conversaService,
            UsuarioService usuarioService) {
        this.conversaService = conversaService;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/criar/{idUsuario1}/{idUsuario2}")
    @ResponseStatus(HttpStatus.CREATED)
    public Conversa criarConversa(@PathVariable Long idUsuario1, @PathVariable Long idUsuario2) {
        try {
            return conversaService.criarConversa(idUsuario1, idUsuario2);
        } catch (RuntimeException e) {
            throw new RuntimeException("Erro ao criar a conversa: " + e.getMessage());
        }
    }

    @DeleteMapping("/deletar-conversa/{idConversa}")
    public Conversa deletarConversa(@PathVariable Long idConversa) {
        return conversaService.deletarConversa(idConversa);
    }

    @GetMapping("/buscar-conversas")
    public List<ConversaDTO> buscarConversas(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Usuário não autenticado.");
        }

        Long idUsuarioLogado = usuarioService.buscarIdPorEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Usuário autenticado não encontrado."));

        return conversaService.buscarConversas(idUsuarioLogado)
                .stream()
                .map(ConversaDTO::from)
                .toList();
    }
}
