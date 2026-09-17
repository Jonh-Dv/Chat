package com.chat.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.chat.dto.UsuarioDTO;
import com.chat.service.AuthService;

@RestController 
@RequestMapping("/auth")
public class AuthController {
    
    @Autowired  
    private AuthService authService;

    @PostMapping(value = "/cadastrar")
    @ResponseStatus(HttpStatus.CREATED)
    public void cadastrarUsuario(@RequestBody UsuarioDTO usuarioDTO){
        authService.cadastrarUsuario(usuarioDTO);
    }

    @GetMapping("/buscar-id-usuario/{email}")
    @ResponseStatus(HttpStatus.OK)
    public Optional<Long> buscarIdPorEmail(@PathVariable String email){
        if(email.isBlank() || email.isEmpty()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O EMAIL NÃO PODE TER ESPAÇOS EM BRANCO OU ESTAR VAZIO");
        }
        return authService.buscarIdUsuario(email);
    }
}
