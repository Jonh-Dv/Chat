package com.chat.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "TB_CONVERSA")
public class Conversa {

    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    @Column(name = "ID_CONVERSA")
    private Long id;

    @Column(name = "DATA_INICIO_CONVERSA")
    private LocalDate dataInicioConversa;
}
