package com.upc.qhurinet.controllers;

import com.upc.qhurinet.dtos.ContactoSoporteDTO;
import com.upc.qhurinet.dtos.CrearTicketDTO;
import com.upc.qhurinet.dtos.EvidenciaTicketDTO;
import com.upc.qhurinet.dtos.FaqDTO;
import com.upc.qhurinet.dtos.TicketSoporteDTO;
import com.upc.qhurinet.services.TicketSoporteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

// Soporte: preguntas frecuentes, tickets y contacto (END-45 a END-49)
@RestController
@CrossOrigin(origins = "${ip.frontend}", allowCredentials = "true", exposedHeaders = "Authorization")
@RequestMapping("/api/v1")
public class TicketSoporteController {
    @Autowired
    private TicketSoporteService ticketSoporteService;

    // END-45
    @GetMapping("/faqs")
    public ResponseEntity<List<FaqDTO>> listarFaqs() {
        return ResponseEntity.ok(ticketSoporteService.listarFaqs());
    }

    // END-46
    @PostMapping("/support-tickets")
    public ResponseEntity<TicketSoporteDTO> crear(@RequestBody CrearTicketDTO crearTicketDTO) {
        TicketSoporteDTO ticket = ticketSoporteService.crear(emailAutenticado(), crearTicketDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(ticket);
    }

    // END-47: estado = abierto | cerrado
    @GetMapping("/support-tickets")
    public ResponseEntity<List<TicketSoporteDTO>> listarMisTickets(
            @RequestParam(value = "estado", required = false) String estado) {
        return ResponseEntity.ok(ticketSoporteService.listarMisTickets(emailAutenticado(), estado));
    }

    // END-48: multipart/form-data con el campo "archivo"
    @PostMapping(value = "/support-tickets/{id}/evidence", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EvidenciaTicketDTO> subirEvidencia(@PathVariable Long id,
            @RequestParam(value = "archivo", required = false) MultipartFile archivo) {
        return ResponseEntity.ok(ticketSoporteService.subirEvidencia(emailAutenticado(), id, archivo));
    }

    // END-49
    @GetMapping("/support/contact")
    public ResponseEntity<ContactoSoporteDTO> obtenerContacto() {
        return ResponseEntity.ok(ticketSoporteService.obtenerContacto());
    }

    private String emailAutenticado() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
