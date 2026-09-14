package com.chat.service;

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
import com.chat.entity.Mensagem;
import com.chat.entity.Usuario;
import com.chat.repository.ConversaRepository;
import com.chat.repository.MensagemRepository;
import com.chat.repository.UsuarioRepository;

@Service
public class MensagemService {

    private static final Logger logger = LoggerFactory.getLogger(MensagemService.class);

    @Autowired
    private MensagemRepository mensagemRepository;

    @Autowired
    private ConversaRepository conversaRepository;

    @Autowired
    private ConversaService conversaService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

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
        Mensagem mensagemSalva = mensagemRepository.save(mensagem);

        simpMessagingTemplate.convertAndSend("/topic/conversa" + mensagemSalva.getIdConversa().getIdConversa(), mensagemSalva);

        return mensagemSalva;

    }

    // Funcionalidade que apaga varias mensagens
    @Transactional
    public void deletarMensagens(Long idConversa) {
        if (idConversa == null || idConversa <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O ID da conversa precisa ser válido.");
        }
        if (!conversaRepository.existsById(idConversa)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversa não encontrada.");
        }

        List<Mensagem> mensagemExcluidas = mensagemRepository.buscarMensagensPorIdConversa(idConversa);

        if (!mensagemExcluidas.isEmpty()) {
            mensagemRepository.deleteAll(mensagemExcluidas);

            // apenas para exibição dos dados que foram excluidos
            for (Mensagem mensagens : mensagemExcluidas) {
                System.out.println(mensagens.getConteudo());
                System.out.println(mensagens.getId());
                System.out.println(mensagens.getDataEnvio());
                System.out.println(mensagens.getRemetente());
            }

        }

    }

    // Funcionalidade que apaga uma unica mensagem
    @Transactional
    public Long deletarMensagem(Long idMensagem) {
        if (idMensagem == null || idMensagem <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O ID da mensagem precisa ser válido.");
        }
        Mensagem mensagem = mensagemRepository.findById(idMensagem)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mensagem não encontrada."));
        Long idConversa = mensagem.getIdConversa().getIdConversa();
        mensagemRepository.delete(mensagem);
        return idConversa;
    }

    @Transactional
    public List<Mensagem> buscarMensagensOuCriarConversa(Long idConversa, Long idEnviando, Long idRecebendo) {
        Conversa conversa = obterOuCriarConversa(idConversa, idEnviando, idRecebendo);
        List<Mensagem> mensagens = mensagemRepository.buscarMensagensPorIdConversa(conversa.getIdConversa());
        if (!mensagens.isEmpty()) {
            logger.debug("A conversa {} possui mensagens.", conversa.getIdConversa());
        }
        return mensagens;
    }

    private Conversa obterOuCriarConversa(Long idConversa, Long idEnviando, Long idRecebendo) {
        if (idConversa == null || idEnviando == null || idRecebendo == null
                || idConversa <= 0 || idEnviando <= 0 || idRecebendo <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Os IDs devem ser positivos e obrigatórios.");
        }

        Long idEsperado = conversaService.gerarIdConversa(idEnviando, idRecebendo);
        if (!idConversa.equals(idEsperado)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O ID da conversa não corresponde aos usuários.");
        }

        return conversaRepository.findById(idConversa)
                .orElseGet(() -> conversaService.criarConversa(idEnviando, idRecebendo));
    }
}
