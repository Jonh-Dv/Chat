package com.chat.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.chat.dto.MensagemDTO;
import com.chat.entity.Mensagem;
import com.chat.service.MensagemService;

@RestController
@RequestMapping("/mensagem")
public class MensagemController {

    @Autowired 
    private MensagemService mensagemService;

    @PostMapping("/salvar-mensagem/{idEnviando}/{idRecebendo}/{conteudo}")
    @ResponseStatus(HttpStatus.CREATED)
    public MensagemDTO salvarMensagem(
        @PathVariable Long idEnviando,
        @PathVariable Long idRecebendo,
        @PathVariable String conteudo
    ) {
        Mensagem mensagem = mensagemService.criarMensagem(idEnviando, idRecebendo, conteudo);
        return new MensagemDTO(
                mensagem.getId(),
                mensagem.getIdConversa().getIdConversa(),
                mensagem.getRemetente().getId(),
                mensagem.getConteudo(),
                mensagem.getDataEnvio());
    }
}
