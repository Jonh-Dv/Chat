package com.chat.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chat.entity.Conversa;

public interface ConversaRepository extends JpaRepository<Conversa, Long> {

    Conversa findByIdConversa(Long idConversa);

    List<Conversa> findByIdRemetenteOrIdDestinatarioOrderByDataInicioConversaDesc(
            Long idRemetente,
            Long idDestinatario);
}
