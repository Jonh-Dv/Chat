package com.chat.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.chat.entity.Conversa;
import com.chat.service.ConversaService;
import com.chat.service.UsuarioService;

@ExtendWith(MockitoExtension.class)
class ConversaControllerTests {

    @Mock
    private ConversaService conversaService;
    @Mock
    private UsuarioService usuarioService;

    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {
        ConversaController controller = new ConversaController(conversaService, usuarioService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void buscaConversasDoUsuarioAutenticado() throws Exception {
        LocalDateTime dataInicio = LocalDateTime.of(2026, 9, 22, 10, 30);
        when(usuarioService.buscarIdPorEmail("usuario@exemplo.com"))
                .thenReturn(Optional.of(1L));
        when(conversaService.buscarConversas(1L))
                .thenReturn(List.of(new Conversa(21L, dataInicio, 1L, 2L)));

        mockMvc.perform(get("/conversa/buscar-conversas")
                        .principal(() -> "usuario@exemplo.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idConversa").value(21))
                .andExpect(jsonPath("$[0].idRemetente").value(1))
                .andExpect(jsonPath("$[0].idDestinatario").value(2));

        verify(usuarioService).buscarIdPorEmail("usuario@exemplo.com");
        verify(conversaService).buscarConversas(1L);
    }

    @Test
    void rejeitaRequisicaoSemUsuarioAutenticado() throws Exception {
        mockMvc.perform(get("/conversa/buscar-conversas"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(usuarioService, conversaService);
    }
}
