package com.chat.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
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
import com.chat.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class MensagemServiceTests {

    @Mock
    private MensagemRepository mensagemRepository;
    @Mock
    private ConversaRepository conversaRepository;
    @Mock
    private ConversaService conversaService;
    @Mock
    private UsuarioRepository usuarioRepository;
    @InjectMocks
    private MensagemService mensagemService;

    @Test
    void salvaMensagemCompletaNaConversaExistente() {
        Usuario remetente = prepararUsuarios();
        Conversa conversa = new Conversa(21L, LocalDateTime.now());
        when(conversaRepository.findById(21L)).thenReturn(Optional.of(conversa));
        when(mensagemRepository.save(any(Mensagem.class))).thenAnswer(invocation -> {
            Mensagem mensagem = invocation.getArgument(0);
            assertNull(mensagem.getId());
            assertSame(conversa, mensagem.getIdConversa());
            assertSame(remetente, mensagem.getRemetente());
            assertEquals("Olá!", mensagem.getConteudo());
            assertNotNull(mensagem.getDataEnvio());
            mensagem.setId(10L);
            return mensagem;
        });

        Mensagem mensagem = mensagemService.criarMensagem(1L, 2L, "Olá!");

        assertEquals(10L, mensagem.getId());
        verify(conversaService, never()).criarConversa(any(), any());
        verify(mensagemRepository).save(mensagem);
    }

    @Test
    void criaConversaComOsDoisUsuariosAntesDeSalvarMensagem() {
        prepararUsuarios();
        Conversa conversa = new Conversa(21L, LocalDateTime.now());
        when(conversaRepository.findById(21L)).thenReturn(Optional.empty());
        when(conversaService.criarConversa(1L, 2L)).thenReturn(conversa);
        when(mensagemRepository.save(any(Mensagem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Mensagem mensagem = mensagemService.criarMensagem(1L, 2L, "Primeira mensagem");

        assertSame(conversa, mensagem.getIdConversa());
        verify(conversaService).criarConversa(1L, 2L);
        verify(mensagemRepository).save(mensagem);
    }

    @Test
    void rejeitaRemetenteInexistenteSemCriarConversaOuMensagem() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException erro = assertThrows(ResponseStatusException.class,
                () -> mensagemService.criarMensagem(1L, 2L, "Olá!"));

        assertEquals(HttpStatus.NOT_FOUND, erro.getStatusCode());
        verifyNoInteractions(conversaRepository, conversaService, mensagemRepository);
    }

    @Test
    void rejeitaDestinatarioInexistenteSemCriarConversaOuMensagem() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(new Usuario()));
        when(usuarioRepository.existsById(2L)).thenReturn(false);

        ResponseStatusException erro = assertThrows(ResponseStatusException.class,
                () -> mensagemService.criarMensagem(1L, 2L, "Olá!"));

        assertEquals(HttpStatus.NOT_FOUND, erro.getStatusCode());
        verifyNoInteractions(conversaRepository, conversaService, mensagemRepository);
    }

    @Test
    void rejeitaConteudoEmBrancoSemAcessarOBanco() {
        ResponseStatusException erro = assertThrows(ResponseStatusException.class,
                () -> mensagemService.criarMensagem(1L, 2L, "   "));

        assertEquals(HttpStatus.BAD_REQUEST, erro.getStatusCode());
        verifyNoInteractions(usuarioRepository, conversaRepository, conversaService, mensagemRepository);
    }

    private Usuario prepararUsuarios() {
        Usuario remetente = new Usuario();
        remetente.setId(1L);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(remetente));
        when(usuarioRepository.existsById(2L)).thenReturn(true);
        when(conversaService.gerarIdConversa(1L, 2L)).thenReturn(21L);
        return remetente;
    }
}
