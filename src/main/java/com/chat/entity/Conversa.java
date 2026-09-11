package com.chat.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


@AllArgsConstructor 
@NoArgsConstructor 
@Entity
@Table(name = "TB_CONVERSA")
public class Conversa {

    @Id 
    @Column(name = "ID_CONVERSA")
    private Long id;

    @Column(name = "DATA_INICIO_CONVERSA")
    private LocalDate dataInicioConversa;
}
