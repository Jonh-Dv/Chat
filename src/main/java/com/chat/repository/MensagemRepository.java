package com.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chat.entity.Mensagem;

public interface MensagemRepository extends JpaRepository<Mensagem, Long> {
    
}
