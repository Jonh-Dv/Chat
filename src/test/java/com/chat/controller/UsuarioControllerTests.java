package com.chat.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import com.chat.entity.Usuario;
import com.chat.service.UsuarioService;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTests {

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private UsuarioController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void configurarController() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void buscaIdPorEmail() throws Exception {
        when(usuarioService.buscarIdPorEmail("usuario@chat.com")).thenReturn(Optional.of(7L));

        mockMvc.perform(get("/usuario/buscar-id-usuario/{email}", "usuario@chat.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(7));
    }

    @Test
    void buscaUsuariosSemExporDadosSensiveis() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(7L);
        usuario.setName("João");
        usuario.setEmail("joao@chat.com");
        usuario.setPassword("hash-que-nao-deve-ser-retornado");

        when(usuarioService.buscarPorNome("João")).thenReturn(List.of(usuario));

        mockMvc.perform(get("/usuario/buscar-usuarios/{nome}", " João "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[0].nome").value("João"))
                .andExpect(jsonPath("$[0].email").doesNotExist())
                .andExpect(jsonPath("$[0].password").doesNotExist());

        verify(usuarioService).buscarPorNome("João");
    }

    @Test
    void respondeNotFoundQuandoNaoHaUsuarios() throws Exception {
        when(usuarioService.buscarPorNome("Inexistente")).thenReturn(List.of());

        mockMvc.perform(get("/usuario/buscar-usuarios/{nome}", "Inexistente"))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejeitaNomeEmBranco() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.buscarUsuarios("   "));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }
}
