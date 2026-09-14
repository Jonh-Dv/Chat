package com.chat.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import com.chat.repository.ConversaRepository;
import com.chat.repository.MensagemRepository;

@ExtendWith(MockitoExtension.class)
class MensagemConsultaServiceTests {

    @Mock
    private MensagemRepository mensagemRepository;
    @Mock
    private ConversaRepository conversaRepository;
    @Mock
    private ConversaService conversaService;
    @InjectMocks
    private MensagemService mensagemService;

    @Test
    void naoRecriaConversaExistenteSemMensagens() {
        when(conversaService.gerarIdConversa(1L, 2L)).thenReturn(21L);
        when(conversaRepository.findById(21L))
                .thenReturn(Optional.of(new Conversa(21L, LocalDateTime.now())));
        when(mensagemRepository.buscarMensagensPorIdConversa(21L)).thenReturn(List.of());

        assertTrue(mensagemService.buscarMensagensOuCriarConversa(21L, 1L, 2L).isEmpty());

        verify(conversaService, never()).criarConversa(any(), any());
    }

    @Test
    void retornaMensagensDaConversaExistente() {
        List<Mensagem> mensagens = List.of(new Mensagem());
        when(conversaService.gerarIdConversa(1L, 2L)).thenReturn(21L);
        when(conversaRepository.findById(21L))
                .thenReturn(Optional.of(new Conversa(21L, LocalDateTime.now())));
        when(mensagemRepository.buscarMensagensPorIdConversa(21L)).thenReturn(mensagens);

        assertSame(mensagens, mensagemService.buscarMensagensOuCriarConversa(21L, 1L, 2L));

        verify(conversaService, never()).criarConversa(any(), any());
    }

    @Test
    void criaConversaInexistenteAntesDeConsultarMensagens() {
        when(conversaService.gerarIdConversa(1L, 2L)).thenReturn(21L);
        when(conversaRepository.findById(21L)).thenReturn(Optional.empty());
        when(conversaService.criarConversa(1L, 2L)).thenReturn(new Conversa(21L, LocalDateTime.now()));
        when(mensagemRepository.buscarMensagensPorIdConversa(21L)).thenAnswer(invocation -> {
            verify(conversaService).criarConversa(1L, 2L);
            return List.of();
        });

        assertTrue(mensagemService.buscarMensagensOuCriarConversa(21L, 1L, 2L).isEmpty());
    }

    @Test
    void rejeitaConversaQueNaoCorrespondeAosUsuarios() {
        when(conversaService.gerarIdConversa(1L, 2L)).thenReturn(21L);

        ResponseStatusException erro = assertThrows(ResponseStatusException.class,
                () -> mensagemService.buscarMensagensOuCriarConversa(99L, 1L, 2L));

        assertEquals(HttpStatus.BAD_REQUEST, erro.getStatusCode());
        verifyNoInteractions(conversaRepository, mensagemRepository);
        verify(conversaService, never()).criarConversa(any(), any());
    }

    @Test
    void rejeitaIdsNulosOuNaoPositivosAntesDeConsultarOBanco() {
        Long[][] idsInvalidos = {
                { null, 1L, 2L }, { 21L, null, 2L }, { 21L, 1L, null },
                { 0L, 1L, 2L }, { 21L, 0L, 2L }, { 21L, 1L, -2L }
        };
        for (Long[] ids : idsInvalidos) {
            ResponseStatusException erro = assertThrows(ResponseStatusException.class,
                    () -> mensagemService.buscarMensagensOuCriarConversa(ids[0], ids[1], ids[2]));
            assertEquals(HttpStatus.BAD_REQUEST, erro.getStatusCode());
        }
        verifyNoInteractions(conversaService, conversaRepository, mensagemRepository);
    }
}
