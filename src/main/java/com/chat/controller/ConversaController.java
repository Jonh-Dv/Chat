package com.chat.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.HttpStatus;

import com.chat.entity.Conversa;
import com.chat.service.ConversaService;

@RestController
@RequestMapping("/conversa")
public class ConversaController {
    @Autowired
    private ConversaService conversaService;

    @PostMapping("/criar/{idUsuario1}/{idUsuario2}")
    @ResponseStatus(HttpStatus.CREATED)
    public Conversa criarConversa(@PathVariable Long idUsuario1, @PathVariable Long idUsuario2) {
        try {
            return conversaService.criarConversa(idUsuario1, idUsuario2);
        } catch (RuntimeException e) {
            throw new RuntimeException("Erro ao criar a conversa: " + e.getMessage());
        }
    }
}
