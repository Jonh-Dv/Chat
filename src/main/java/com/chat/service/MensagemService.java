package com.chat.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.chat.entity.Conversa;
import com.chat.entity.Mensagem;
import com.chat.entity.Usuario;
import com.chat.repository.ConversaRepository;
import com.chat.repository.MensagemRepository;
import com.chat.repository.UsuarioRepository;

@Service 
public class MensagemService {
    
    @Autowired 
    private MensagemRepository mensagemRepository;

    @Autowired
    private ConversaRepository conversaRepository;
    
    @Autowired 
    private ConversaService conversaService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional
    public Mensagem criarMensagem(Long idEnviando, Long idRecebendo, String conteudo) {
        if (conteudo == null || conteudo.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O conteúdo da mensagem é obrigatório.");
        }

        Usuario remetente = usuarioRepository.findById(idEnviando)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Remetente não encontrado."));

        if (!usuarioRepository.existsById(idRecebendo)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Destinatário não encontrado.");
        }

        Long idConversa = conversaService.gerarIdConversa(idEnviando, idRecebendo);
        Conversa conversa = conversaRepository.findById(idConversa)
                .orElseGet(() -> conversaService.criarConversa(idEnviando, idRecebendo));

        Mensagem mensagem = new Mensagem(conversa, remetente, conteudo);
        return mensagemRepository.save(mensagem);
    }
}
