package com.chat.service;

import java.security.Principal;
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
    public Mensagem criarMensagem(Long idEnviando, Long idRecebendo, String conteudo, String mensagemVisualizada) {

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

        Mensagem mensagem = new Mensagem(conversa, remetente, conteudo, mensagemVisualizada);
        Mensagem mensagemSalva = mensagemRepository.save(mensagem);

        simpMessagingTemplate.convertAndSend("/topic/conversa" + mensagemSalva.getIdConversa().getIdConversa(),
                mensagemSalva);

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
        }

    }

    // Funcionalidade que apaga uma unica mensagem
    @Transactional
    public Long deletarMensagem(Long idMensagem, Long idUsuarioLogado) {
        if (idMensagem == null || idMensagem <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O ID da mensagem precisa ser válido.");
        }
        Mensagem mensagem = mensagemRepository.findById(idMensagem)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mensagem não encontrada."));

        if (mensagem.getRemetente().getId().equals(idUsuarioLogado)) {
            logger.info("USUÁRIO TEM PERMISSÃO PARA APAGAR SUA MENSAGEM");
        } else {
            logger.info("USUÁRIO NÃO TEM PERMISSÃO PARA APAGAR SUA CONVERSA");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O USUÁRIO SÓ PODE APAGAR SUA PRÓPRIA MENSAGEM");
        }

        Long idConversa = mensagem.getIdConversa().getIdConversa();
        mensagemRepository.delete(mensagem);
        return idConversa;
    }

    @Transactional
    public List<Mensagem> buscarMensagensOuCriarConversa(Long idConversa, Long idEnviando, Long idRecebendo,
            Principal principal) {
        validarIdsDaConversa(idConversa, idEnviando, idRecebendo);
        Long idUsuarioAutenticado = obterIdUsuarioAutenticado(principal);
        validarParticipanteInformado(idUsuarioAutenticado, idEnviando, idRecebendo);

        Conversa conversa = obterOuCriarConversa(idConversa, idEnviando, idRecebendo);
        validarParticipantesDaConversa(conversa, idUsuarioAutenticado, idEnviando, idRecebendo);

        List<Mensagem> mensagens = mensagemRepository.buscarMensagensPorIdConversa(conversa.getIdConversa());
        if (!mensagens.isEmpty()) {
            definirMensagensComoVisualizadas(mensagens, idUsuarioAutenticado);
            logger.debug("A conversa {} possui mensagens.", conversa.getIdConversa());
        }
        return mensagens;
    }

    private Long obterIdUsuarioAutenticado(Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado.");
        }

        return usuarioRepository.findIdByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Usuário autenticado não encontrado."));
    }

    private void validarParticipanteInformado(Long idUsuarioAutenticado, Long idEnviando, Long idRecebendo) {
        if (!idUsuarioAutenticado.equals(idEnviando) && !idUsuarioAutenticado.equals(idRecebendo)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "O usuário autenticado não participa desta conversa.");
        }
    }

    private void validarParticipantesDaConversa(
            Conversa conversa,
            Long idUsuarioAutenticado,
            Long idEnviando,
            Long idRecebendo) {
        Long idRemetente = conversa.getIdRemetente();
        Long idDestinatario = conversa.getIdDestinatario();

        boolean participantesCorrespondem =
                (idEnviando.equals(idRemetente) && idRecebendo.equals(idDestinatario))
                        || (idEnviando.equals(idDestinatario) && idRecebendo.equals(idRemetente));
        boolean usuarioParticipaDaConversa =
                idUsuarioAutenticado.equals(idRemetente) || idUsuarioAutenticado.equals(idDestinatario);

        if (!participantesCorrespondem || !usuarioParticipaDaConversa) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "O usuário autenticado não participa desta conversa.");
        }
    }

    private Conversa obterOuCriarConversa(Long idConversa, Long idEnviando, Long idRecebendo) {
        Long idEsperado = conversaService.gerarIdConversa(idEnviando, idRecebendo);
        if (!idConversa.equals(idEsperado)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O ID da conversa não corresponde aos usuários.");
        }

        return conversaRepository.findById(idConversa)
                .orElseGet(() -> conversaService.criarConversa(idEnviando, idRecebendo));
    }

    private void validarIdsDaConversa(Long idConversa, Long idEnviando, Long idRecebendo) {
        if (idConversa == null || idEnviando == null || idRecebendo == null
                || idConversa <= 0 || idEnviando <= 0 || idRecebendo <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Os IDs devem ser positivos e obrigatórios.");
        }
    }

    private void definirMensagensComoVisualizadas(
            List<Mensagem> mensagens,
            Long idUsuario) {
        mensagens.stream()
                .filter(mensagem -> !mensagem.getRemetente().getId().equals(idUsuario))
                .filter(mensagem -> !"SIM".equals(mensagem.getMensagemVisualizada()))
                .forEach(mensagem -> {
                    mensagem.setMensagemVisualizada("SIM");
                    logger.info(
                            "Mensagem {} marcada como visualizada",
                            mensagem.getId());
                });
    }
}
