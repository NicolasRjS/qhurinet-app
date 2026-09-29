package com.upc.qhurinet.services;

import com.upc.qhurinet.dtos.EnviarMensajeDTO;
import com.upc.qhurinet.dtos.MensajeDTO;

import java.util.List;

public interface MensajeService {
    public List<MensajeDTO> listar(String email, Long solicitudId);                                // END-32
    public MensajeDTO enviar(String email, Long solicitudId, EnviarMensajeDTO enviarMensajeDTO);   // END-33
}
