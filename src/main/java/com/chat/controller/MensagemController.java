package com.chat.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping("/buscar-mensagens/{idConversa}/{idEnviando}/{idRecebendo}")
    public List<MensagemDTO> buscarMensagens(
        @PathVariable Long idConversa,
        @PathVariable Long idEnviando,
        @PathVariable Long idRecebendo
    ){
        List<Mensagem> mensagens = mensagemService.buscarMensagensOuCriarConversa(idConversa, idEnviando, idRecebendo);

        List<MensagemDTO> mensagensRetorno = new ArrayList<>();

        for(Mensagem mensagem : mensagens){
            MensagemDTO mensagemDtoProvisorio = new MensagemDTO();
            mensagemDtoProvisorio.setConteudo(mensagem.getConteudo());
            mensagemDtoProvisorio.setDataEnvio(mensagem.getDataEnvio());
            mensagemDtoProvisorio.setId(mensagem.getId());
            mensagemDtoProvisorio.setIdConversa(mensagem.getIdConversa().getIdConversa());
            mensagemDtoProvisorio.setIdRemetente(mensagem.getRemetente().getId());

            mensagensRetorno.add(mensagemDtoProvisorio);
        }

        return mensagensRetorno;
    }
}
