package com.chat.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
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

}
