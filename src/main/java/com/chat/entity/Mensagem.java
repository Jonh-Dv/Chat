package com.chat.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "TB_MENSAGEM")
public class Mensagem {
    
    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    @Column(name = "ID_MENSAGEM")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ID_CONVERSA")
    private Conversa idConversa;

    @ManyToOne
    @JoinColumn(name = "ID_REMETENTE")
    private Usuario remetente;

    @Column(name = "CONTEUDO")
    private String conteudo;

    @Column(name = "DATA_ENVIO")
    private LocalDate dataEnvio;
}
