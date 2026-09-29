package com.upc.qhurinet.services;

import com.upc.qhurinet.dtos.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TicketSoporteService {
    public List<FaqDTO> listarFaqs();                                                      // END-45
    public TicketSoporteDTO crear(String email, CrearTicketDTO crearTicketDTO);            // END-46
    public List<TicketSoporteDTO> listarMisTickets(String email, String estado);           // END-47
    public EvidenciaTicketDTO subirEvidencia(String email, Long id, MultipartFile archivo); // END-48
    public ContactoSoporteDTO obtenerContacto();                                           // END-49
}
