package com.chat.controller;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import com.chat.service.MensagemService;

@ExtendWith(MockitoExtension.class)
class MensagemControllerTests {

    @Mock
    private MensagemService mensagemService;
    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;
    @InjectMocks
    private MensagemController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void configurarController() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void notificaExclusaoNoTopicoDaConversaComIdDaMensagem() throws Exception {
        when(mensagemService.deletarMensagem(10L)).thenReturn(21L);

        mockMvc.perform(delete("/mensagem/deletar-mensagem/10"))
                .andExpect(status().isNoContent());

        Map<String, ?> evento = Map.of("tipo", "MENSAGEM_DELETADA", "idMensagem", 10L);
        verify(simpMessagingTemplate).convertAndSend("/topic/conversa21", evento);
    }

    @Test
    void notificaLimpezaNoTopicoDaConversa() throws Exception {
        mockMvc.perform(delete("/mensagem/limpar-conversa/21"))
                .andExpect(status().isNoContent());

        verify(mensagemService).deletarMensagens(21L);
        verify(simpMessagingTemplate).convertAndSend("/topic/conversa21", "CONVERSA_LIMPA");
    }

    @Test
    void naoNotificaQuandoMensagemNaoExiste() throws Exception {
        when(mensagemService.deletarMensagem(10L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Mensagem não encontrada."));

        mockMvc.perform(delete("/mensagem/deletar-mensagem/10"))
                .andExpect(status().isNotFound());

        verifyNoInteractions(simpMessagingTemplate);
    }

    @Test
    void naoNotificaQuandoConversaNaoExiste() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversa não encontrada."))
                .when(mensagemService).deletarMensagens(21L);

        mockMvc.perform(delete("/mensagem/limpar-conversa/21"))
                .andExpect(status().isNotFound());

        verifyNoInteractions(simpMessagingTemplate);
    }

    @Test
    void rejeitaIdsInvalidosSemExcluirOuNotificar() throws Exception {
        mockMvc.perform(delete("/mensagem/deletar-mensagem/0"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(delete("/mensagem/limpar-conversa/-1"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(mensagemService, simpMessagingTemplate);
    }
}
