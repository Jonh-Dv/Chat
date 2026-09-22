package com.chat.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.chat.entity.Conversa;
import com.chat.repository.ConversaRepository;
import com.chat.repository.MensagemRepository;

@Service
public class ConversaService {

    @Autowired
    private ConversaRepository conversaRepository;

    @Autowired
    private MensagemRepository mensagemRepository;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    private static final Logger logger = LoggerFactory.getLogger(ConversaService.class);

    public Conversa criarConversa(Long idUsuario1, Long idUsuario2) {
        Long maior = Math.max(idUsuario1, idUsuario2);
        Long menor = Math.min(idUsuario1, idUsuario2);

        String mediador = maior.toString() + menor.toString();

        Long idConversa = Long.parseLong(mediador);
        LocalDateTime dataCriacao = LocalDateTime.now();

        System.out.println("ID da conversa: " + idConversa);

        if (conversaRepository.findById(idConversa).isPresent()) {
            throw new RuntimeException("Conversa já existe entre os usuários " + idUsuario1 + " e " + idUsuario2);
        } else {
            Conversa conversa = new Conversa(idConversa, dataCriacao, idUsuario1, idUsuario2);
            conversaRepository.save(conversa);
            return conversa;
        }
    }

    @Transactional
    public Conversa deletarConversa(Long idConversa) {
        Conversa conversa = conversaRepository.findById(idConversa)
                .orElseThrow(() -> new RuntimeException("Conversa não encontrada com o ID: " + idConversa));

        mensagemRepository.deletarPorIdConversa(idConversa);
        conversaRepository.delete(conversa);

        simpMessagingTemplate.convertAndSend("/topic/deletar-conversa", conversa);
        return conversa;
    }

    public Long gerarIdConversa(Long idUsuario1, Long idUsuario2) {
        Long maior = Math.max(idUsuario1, idUsuario2);
        Long menor = Math.min(idUsuario1, idUsuario2);

        String mediador = maior.toString() + menor.toString();

        Long idConversa = Long.parseLong(mediador);
        System.out.println("ID da conversa: " + idConversa);
        return idConversa;
    }

    @Transactional(readOnly = true)
    public List<Conversa> buscarConversas(Long idUsuarioLogado) {
        if (idUsuarioLogado == null || idUsuarioLogado <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O ID do usuário deve ser válido.");
        }

        List<Conversa> conversas = conversaRepository
                .findByIdRemetenteOrIdDestinatarioOrderByDataInicioConversaDesc(
                        idUsuarioLogado,
                        idUsuarioLogado);

        logger.info(
                "{} conversas encontradas para o usuário {}",
                conversas.size(),
                idUsuarioLogado);

        conversas.forEach(conversa -> logger.debug(
                "Conversa encontrada: id={}",
                conversa.getIdConversa()));

        return conversas;
    }
}
