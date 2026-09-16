package com.chat.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.chat.entity.Mensagem;

public interface MensagemRepository extends JpaRepository<Mensagem, Long> {

    @Query("SELECT c FROM Mensagem c WHERE c.idConversa.idConversa = :idConversa")
    List<Mensagem> buscarMensagensPorIdConversa(@Param("idConversa") Long idConversa);

    @Query("SELECT m.remetente.id FROM Mensagem m WHERE m.id = :idMensagem")
    Long buscarIdRemetentePorIdMensagem(@Param("idMensagem") Long idMensagem);

    @Modifying
    @Query("""
            DELETE FROM Mensagem m
            WHERE m.idConversa.idConversa = :idConversa
            """)
    int deletarPorIdConversa(@Param("idConversa") Long idConversa);
}
