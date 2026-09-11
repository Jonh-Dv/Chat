package com.chat.service;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chat.entity.Conversa;
import com.chat.repository.ConversaRepository;

@Service
public class ConversaService {

    @Autowired
    private ConversaRepository conversaRepository;

    public Conversa criarConversa(Long idUsuario1, Long idUsuario2) {
        Long maior = Math.max(idUsuario1, idUsuario2);
        Long menor = Math.min(idUsuario1, idUsuario2);

        String mediador = maior.toString() + menor.toString();

        Long idConversa = Long.parseLong(mediador);
        LocalDate dataCriacao = LocalDate.now();

        System.out.println("ID da conversa: " + idConversa);

        if (conversaRepository.findById(idConversa).isPresent()) {
            throw new RuntimeException("Conversa já existe entre os usuários " + idUsuario1 + " e " + idUsuario2);
        } else {
            Conversa conversa = new Conversa(idConversa, dataCriacao);
            conversaRepository.save(conversa);
            return conversa;
        }
    }

    public Conversa deletarConversa(Long idConversa){
        Conversa conversa = conversaRepository.findById(idConversa)
                .orElseThrow(() -> new RuntimeException("Conversa não encontrada com o ID: " + idConversa));
        conversaRepository.delete(conversa);
        return conversa;
    }
}
