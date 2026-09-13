package com.chat.dto;

import java.time.LocalDateTime;

public record MensagemDTO(
    Long id,
    Long idConversa,
    Long idRemetente,
    String conteudo,
    LocalDateTime dataEnvio
) {}
