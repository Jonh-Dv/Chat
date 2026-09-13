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

    @Id 
    @Column(name = "ID_CONVERSA")
    private Long idConversa;

    @Column(name = "DATA_INICIO_CONVERSA")
    private LocalDateTime dataInicioConversa;
}
