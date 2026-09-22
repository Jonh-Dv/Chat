package com.chat.dto;

import java.time.LocalDateTime;

import com.chat.entity.Conversa;

public record ConversaDTO(
        Long idConversa,
        LocalDateTime dataInicioConversa,
        Long idRemetente,
        Long idDestinatario) {

    public static ConversaDTO from(Conversa conversa) {
        return new ConversaDTO(
                conversa.getIdConversa(),
                conversa.getDataInicioConversa(),
                conversa.getIdRemetente(),
                conversa.getIdDestinatario());
    }
}
