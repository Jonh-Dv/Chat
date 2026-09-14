package com.chat.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.chat.entity.Conversa;
import com.chat.entity.Mensagem;
import com.chat.entity.Usuario;
import com.chat.repository.ConversaRepository;
import com.chat.repository.MensagemRepository;

@ExtendWith(MockitoExtension.class)
class MensagemExclusaoServiceTests {

    @Mock
    private MensagemRepository mensagemRepository;
    @Mock
    private ConversaRepository conversaRepository;
    @InjectMocks
    private MensagemService mensagemService;

    @Test
    void rejeitaIdsInvalidosNasDuasOperacoesSemAcessarOBanco() {
        for (Long id : new Long[] { null, 0L, -1L }) {
            ResponseStatusException erroMensagem = assertThrows(ResponseStatusException.class,
                    () -> mensagemService.deletarMensagem(id, 1L));
            ResponseStatusException erroConversa = assertThrows(ResponseStatusException.class,
                    () -> mensagemService.deletarMensagens(id));
            assertEquals(HttpStatus.BAD_REQUEST, erroMensagem.getStatusCode());
            assertEquals(HttpStatus.BAD_REQUEST, erroConversa.getStatusCode());
        }
        verifyNoInteractions(mensagemRepository, conversaRepository);
    }

    @Test
    void retorna404ParaMensagemInexistenteSemExcluir() {
        when(mensagemRepository.findById(10L)).thenReturn(Optional.empty());

        ResponseStatusException erro = assertThrows(ResponseStatusException.class,
                () -> mensagemService.deletarMensagem(10L, 1L));

        assertEquals(HttpStatus.NOT_FOUND, erro.getStatusCode());
        verify(mensagemRepository, never()).delete(any(Mensagem.class));
    }

    @Test
    void excluiMensagemExistente() {
        Mensagem mensagem = new Mensagem();
        Usuario remetente = new Usuario();
        remetente.setId(1L);
        mensagem.setRemetente(remetente);
        mensagem.setIdConversa(new Conversa(21L, LocalDateTime.now()));
        when(mensagemRepository.findById(10L)).thenReturn(Optional.of(mensagem));

        assertEquals(21L, mensagemService.deletarMensagem(10L, 1L));

        verify(mensagemRepository).delete(mensagem);
        verify(mensagemRepository, never()).buscarIdRemetentePorIdMensagem(any());
    }

    @Test
    void impedeExclusaoDeMensagemDeOutroUsuarioSemConsultaExtra() {
        Mensagem mensagem = new Mensagem();
        Usuario remetente = new Usuario();
        remetente.setId(1L);
        mensagem.setRemetente(remetente);
        when(mensagemRepository.findById(10L)).thenReturn(Optional.of(mensagem));

        ResponseStatusException erro = assertThrows(ResponseStatusException.class,
                () -> mensagemService.deletarMensagem(10L, 2L));

        assertEquals(HttpStatus.BAD_REQUEST, erro.getStatusCode());
        verify(mensagemRepository, never()).delete(any(Mensagem.class));
        verify(mensagemRepository, never()).buscarIdRemetentePorIdMensagem(any());
    }

    @Test
    void retorna404ParaConversaInexistenteSemConsultarMensagens() {
        when(conversaRepository.existsById(21L)).thenReturn(false);

        ResponseStatusException erro = assertThrows(ResponseStatusException.class,
                () -> mensagemService.deletarMensagens(21L));

        assertEquals(HttpStatus.NOT_FOUND, erro.getStatusCode());
        verifyNoInteractions(mensagemRepository);
    }

    @Test
    void permiteLimparConversaQueJaEstaVazia() {
        when(conversaRepository.existsById(21L)).thenReturn(true);
        when(mensagemRepository.buscarMensagensPorIdConversa(21L)).thenReturn(List.of());

        mensagemService.deletarMensagens(21L);

        verify(mensagemRepository, never()).deleteAll(any());
    }

    @Test
    void limpaConversaExcluindoSomenteAsMensagensConsultadas() {
        List<Mensagem> mensagens = List.of(new Mensagem(), new Mensagem());
        when(conversaRepository.existsById(21L)).thenReturn(true);
        when(mensagemRepository.buscarMensagensPorIdConversa(21L)).thenReturn(mensagens);

        mensagemService.deletarMensagens(21L);

        verify(mensagemRepository).deleteAll(mensagens);
        verify(conversaRepository, never()).deleteById(any());
        verify(conversaRepository, never()).delete(any());
    }
}
