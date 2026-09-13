package com.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chat.entity.Conversa;

public interface ConversaRepository extends JpaRepository<Conversa, Long> {

    Conversa findByIdConversa(Long idConversa);

}
