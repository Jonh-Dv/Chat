package com.chat.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.security.Principal;
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
import com.chat.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class MensagemConsultaServiceTests {

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

    private final Principal principal = () -> "usuario@chat.com";

    @Test
    void naoRecriaConversaExistenteSemMensagens() {
        prepararUsuarioAutenticado(1L);
        when(conversaService.gerarIdConversa(1L, 2L)).thenReturn(21L);
        when(conversaRepository.findById(21L))
                .thenReturn(Optional.of(criarConversa(21L, 1L, 2L)));
        when(mensagemRepository.buscarMensagensPorIdConversa(21L)).thenReturn(List.of());

        assertTrue(mensagemService.buscarMensagensOuCriarConversa(21L, 1L, 2L, principal).isEmpty());

        verify(conversaService, never()).criarConversa(any(), any());
    }

    @Test
    void retornaMensagensDaConversaExistente() {
        prepararUsuarioAutenticado(1L);
        Usuario remetente = new Usuario();
        remetente.setId(1L);
        List<Mensagem> mensagens = List.of(new Mensagem(null, null, remetente, "Olá", LocalDateTime.now(), null));
        when(conversaService.gerarIdConversa(1L, 2L)).thenReturn(21L);
        when(conversaRepository.findById(21L))
                .thenReturn(Optional.of(criarConversa(21L, 1L, 2L)));
        when(mensagemRepository.buscarMensagensPorIdConversa(21L)).thenReturn(mensagens);

        assertSame(mensagens, mensagemService.buscarMensagensOuCriarConversa(21L, 1L, 2L, principal));

        verify(conversaService, never()).criarConversa(any(), any());
    }

    @Test
    void marcaComoVisualizadasSomenteAsMensagensRecebidas() {
        prepararUsuarioAutenticado(1L);
        Conversa conversa = criarConversa(21L, 1L, 2L);
        Usuario usuarioAutenticado = new Usuario();
        usuarioAutenticado.setId(1L);
        Usuario outroUsuario = new Usuario();
        outroUsuario.setId(2L);
        Mensagem mensagemEnviada = new Mensagem(10L, conversa, usuarioAutenticado, "Enviada", LocalDateTime.now(), null);
        Mensagem mensagemRecebida = new Mensagem(11L, conversa, outroUsuario, "Recebida", LocalDateTime.now(), null);

        when(conversaService.gerarIdConversa(1L, 2L)).thenReturn(21L);
        when(conversaRepository.findById(21L)).thenReturn(Optional.of(conversa));
        when(mensagemRepository.buscarMensagensPorIdConversa(21L))
                .thenReturn(List.of(mensagemEnviada, mensagemRecebida));

        mensagemService.buscarMensagensOuCriarConversa(21L, 1L, 2L, principal);

        assertNull(mensagemEnviada.getMensagemVisualizada());
        assertEquals("SIM", mensagemRecebida.getMensagemVisualizada());
    }

    @Test
    void criaConversaInexistenteAntesDeConsultarMensagens() {
        prepararUsuarioAutenticado(1L);
        when(conversaService.gerarIdConversa(1L, 2L)).thenReturn(21L);
        when(conversaRepository.findById(21L)).thenReturn(Optional.empty());
        when(conversaService.criarConversa(1L, 2L)).thenReturn(criarConversa(21L, 1L, 2L));
        when(mensagemRepository.buscarMensagensPorIdConversa(21L)).thenAnswer(invocation -> {
            verify(conversaService).criarConversa(1L, 2L);
            return List.of();
        });

        assertTrue(mensagemService.buscarMensagensOuCriarConversa(21L, 1L, 2L, principal).isEmpty());
    }

    @Test
    void rejeitaConversaQueNaoCorrespondeAosUsuarios() {
        prepararUsuarioAutenticado(1L);
        when(conversaService.gerarIdConversa(1L, 2L)).thenReturn(21L);

        ResponseStatusException erro = assertThrows(ResponseStatusException.class,
                () -> mensagemService.buscarMensagensOuCriarConversa(99L, 1L, 2L, principal));

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
                    () -> mensagemService.buscarMensagensOuCriarConversa(ids[0], ids[1], ids[2], principal));
            assertEquals(HttpStatus.BAD_REQUEST, erro.getStatusCode());
        }
        verifyNoInteractions(usuarioRepository, conversaService, conversaRepository, mensagemRepository);
    }

    @Test
    void impedeUsuarioAutenticadoDeLerConversaDeTerceiros() {
        prepararUsuarioAutenticado(3L);

        ResponseStatusException erro = assertThrows(ResponseStatusException.class,
                () -> mensagemService.buscarMensagensOuCriarConversa(21L, 1L, 2L, principal));

        assertEquals(HttpStatus.FORBIDDEN, erro.getStatusCode());
        verifyNoInteractions(conversaService, conversaRepository, mensagemRepository);
    }

    @Test
    void rejeitaConversaPersistidaComParticipantesDiferentes() {
        prepararUsuarioAutenticado(12L);
        when(conversaService.gerarIdConversa(12L, 34L)).thenReturn(3412L);
        when(conversaRepository.findById(3412L))
                .thenReturn(Optional.of(criarConversa(3412L, 2L, 341L)));

        ResponseStatusException erro = assertThrows(ResponseStatusException.class,
                () -> mensagemService.buscarMensagensOuCriarConversa(3412L, 12L, 34L, principal));

        assertEquals(HttpStatus.FORBIDDEN, erro.getStatusCode());
        verifyNoInteractions(mensagemRepository);
    }

    @Test
    void rejeitaConsultaSemUsuarioAutenticado() {
        ResponseStatusException erro = assertThrows(ResponseStatusException.class,
                () -> mensagemService.buscarMensagensOuCriarConversa(21L, 1L, 2L, null));

        assertEquals(HttpStatus.UNAUTHORIZED, erro.getStatusCode());
        verifyNoInteractions(usuarioRepository, conversaService, conversaRepository, mensagemRepository);
    }

    private void prepararUsuarioAutenticado(Long idUsuario) {
        when(usuarioRepository.findIdByEmail("usuario@chat.com")).thenReturn(Optional.of(idUsuario));
    }

    private Conversa criarConversa(Long idConversa, Long idUsuario1, Long idUsuario2) {
        return new Conversa(idConversa, LocalDateTime.now(), idUsuario1, idUsuario2);
    }
}
