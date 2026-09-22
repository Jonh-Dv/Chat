package com.chat.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.chat.entity.Conversa;
import com.chat.repository.ConversaRepository;
import com.chat.repository.MensagemRepository;

@ExtendWith(MockitoExtension.class)
class ConversaServiceTests {

    @Mock
    private ConversaRepository conversaRepository;
    @Mock
    private MensagemRepository mensagemRepository;
    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;
    @InjectMocks
    private ConversaService conversaService;

    @Test
    void buscaConversasEmQueUsuarioParticipa() {
        List<Conversa> conversas = List.of(
                new Conversa(21L, LocalDateTime.now(), 2L, 1L),
                new Conversa(31L, LocalDateTime.now().minusDays(1), 1L, 3L));
        when(conversaRepository
                .findByIdRemetenteOrIdDestinatarioOrderByDataInicioConversaDesc(1L, 1L))
                .thenReturn(conversas);

        List<Conversa> resultado = conversaService.buscarConversas(1L);

        assertSame(conversas, resultado);
        verify(conversaRepository)
                .findByIdRemetenteOrIdDestinatarioOrderByDataInicioConversaDesc(1L, 1L);
    }

    @Test
    void retornaListaVaziaQuandoUsuarioNaoPossuiConversas() {
        when(conversaRepository
                .findByIdRemetenteOrIdDestinatarioOrderByDataInicioConversaDesc(1L, 1L))
                .thenReturn(List.of());

        List<Conversa> resultado = conversaService.buscarConversas(1L);

        assertEquals(List.of(), resultado);
    }

    @Test
    void rejeitaIdDeUsuarioInvalido() {
        assertThrows(
                ResponseStatusException.class,
                () -> conversaService.buscarConversas(0L));

        verifyNoInteractions(conversaRepository);
    }
}
