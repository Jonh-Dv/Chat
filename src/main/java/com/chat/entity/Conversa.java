package com.chat.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@AllArgsConstructor 
@Getter
@NoArgsConstructor 
@Entity
@Table(name = "TB_CONVERSA")
public class Conversa {

    public Conversa(Long idConversa, LocalDateTime dataInicioConversa) {
        this.idConversa = idConversa;
        this.dataInicioConversa = dataInicioConversa;
    }

    @Id 
    @Column(name = "ID_CONVERSA")
    private Long idConversa;

    @Column(name = "DATA_INICIO_CONVERSA")
    private LocalDateTime dataInicioConversa;

    @Column(name = "ID_REMETENTE")
    private Long idRemetente;

    @Column(name = "ID_DESTINATARIO")
    private Long idDestinatario;
}
