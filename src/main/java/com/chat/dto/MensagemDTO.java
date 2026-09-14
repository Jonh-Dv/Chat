package com.chat.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MensagemDTO {
    private Long id;
    private Long idConversa;
    private Long idRemetente;
    private String conteudo;
    private LocalDateTime dataEnvio;
}
