package com.chat.dto;

import java.time.LocalDateTime;

public record ConversaDTO(
    Long idConversa,
    LocalDateTime dataInicioConversa
) {}
