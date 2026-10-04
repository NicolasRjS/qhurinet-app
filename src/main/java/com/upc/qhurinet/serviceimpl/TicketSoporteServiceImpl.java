package com.upc.qhurinet.serviceimpl;

import com.upc.qhurinet.dtos.ContactoSoporteDTO;
import com.upc.qhurinet.dtos.CrearTicketDTO;
import com.upc.qhurinet.dtos.EvidenciaTicketDTO;
import com.upc.qhurinet.dtos.FaqDTO;
import com.upc.qhurinet.dtos.TicketSoporteDTO;
import com.upc.qhurinet.entities.TicketSoporte;
import com.upc.qhurinet.entities.Usuario;
import com.upc.qhurinet.repositories.TicketSoporteRepositorio;
import com.upc.qhurinet.services.AlmacenamientoService;
import com.upc.qhurinet.services.SolicitudRecoleccionService;
import com.upc.qhurinet.services.TicketSoporteService;
import com.upc.qhurinet.services.UsuarioService;

import jakarta.transaction.Transactional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class TicketSoporteServiceImpl implements TicketSoporteService {

    // Valores admitidos en tickets_soporte.categoria y .estado
    public static final List<String> CATEGORIAS =
            List.of(
                    "problema_con_recolector",
                    "problema_con_generador",
                    "material_no_coincide",
                    "error_en_plataforma",
                    "otro");

    public static final String ABIERTO = "abierto";

    public static final String CERRADO = "cerrado";

    // tickets_soporte.asunto varchar(150)
    private static final int ASUNTO_MAX = 150;

    private static final String CARPETA_EVIDENCIAS = "evidencias_tickets";

    // US 12-EP2
    private static final List<String> FORMATOS_IMAGEN = List.of("jpg", "jpeg", "png", "webp");

    // Contenido basado en los flujos implementados (US 38).
    private static final List<FaqDTO> FAQS =
            List.of(
                    new FaqDTO(
                            "¿Qué hago si la contraparte no llega?",
                            "Escribe en el chat; puedes reprogramar o cancelar una recolección"
                                + " abierta. Si hay un problema, crea un ticket."),
                    new FaqDTO(
                            "¿Quién recoge el material?",
                            "Solo el recolector asignado puede confirmar tu entrega. Consulta su"
                                + " perfil y reputación desde la recolección."),
                    new FaqDTO(
                            "¿Cómo publico material reciclable?",
                            "Inicia sesión como generador con una cuenta activa y publica una"
                                + " categoría, cantidad, dirección y fecha de disponibilidad. Cada"
                                + " publicación corresponde a un solo material."),
                    new FaqDTO(
                            "¿Cómo reclamo un anuncio?",
                            "Como recolector, elige una publicación disponible en el mapa y"
                                + " selecciona reclamar. Luego podrás coordinar el retiro por el"
                                + " chat de esa recolección."),
                    new FaqDTO(
                            "¿Cómo funciona el código QR?",
                            "Cuando la recolección esté coordinada, el generador muestra el código"
                                + " QR y el recolector asignado lo escanea para confirmar la"
                                + " entrega."),
                    new FaqDTO(
                            "¿Puedo reprogramar o cancelar una recolección?",
                            "Abre el detalle de la recolección para reprogramarla o cancelarla."
                                + " Cualquiera de las dos partes puede hacerlo; al cancelar, indica"
                                + " el motivo y la publicación vuelve a estar disponible."),
                    new FaqDTO(
                            "¿Cuándo puedo calificar el servicio?",
                            "Cuando la entrega figure como ejecutada, el generador puede calificar"
                                + " al recolector de una a cinco estrellas."),
                    new FaqDTO(
                            "¿Para qué sirve verificar mis documentos?",
                            "La verificación ayuda a confirmar la identidad de la cuenta. Sube un"
                                + " documento admitido y revisa su estado en tu perfil."),
                    new FaqDTO(
                            "¿Qué métodos de pago puedo acordar?",
                            "Puedes acordar tarjeta, Yape, Plin, transferencia o efectivo. Confirma"
                                + " el monto y el método con la otra parte antes del retiro."),
                    new FaqDTO(
                            "¿Cómo abro un ticket de soporte?",
                            "Entra a soporte, elige la categoría y escribe un asunto y una"
                                + " descripción. Puedes vincularlo a una recolección y adjuntar"
                                + " evidencia."));

    @Value("${soporte.telefono}")
    private String telefonoSoporte;

    @Value("${soporte.horario}")
    private String horarioSoporte;

    @Autowired
    private TicketSoporteRepositorio ticketSoporteRepositorio;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private SolicitudRecoleccionService solicitudRecoleccionService;

    @Autowired
    private AlmacenamientoService almacenamientoService;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public List<FaqDTO> listarFaqs() {
        return FAQS;
    }

    @Transactional
    @Override
    public TicketSoporteDTO crear(String email, CrearTicketDTO crearTicketDTO) {
        List<String> errores = new ArrayList<>();
        if (crearTicketDTO.getCategoria() == null
                || !CATEGORIAS.contains(crearTicketDTO.getCategoria())) {
            errores.add(
                    "categoria: es obligatoria y debe ser una de " + String.join(", ", CATEGORIAS));
        }
        if (crearTicketDTO.getAsunto() == null || crearTicketDTO.getAsunto().isBlank()) {
            errores.add("asunto: es obligatorio");
        } else if (crearTicketDTO.getAsunto().trim().length() > ASUNTO_MAX) {
            errores.add("asunto: no puede superar los " + ASUNTO_MAX + " caracteres");
        }
        if (crearTicketDTO.getDescripcion() == null || crearTicketDTO.getDescripcion().isBlank()) {
            errores.add("descripcion: es obligatoria");
        }
        if (!errores.isEmpty()) {
            throw new IllegalArgumentException(String.join(", ", errores));
        }
        TicketSoporte ticket = new TicketSoporte();
        ticket.setUsuario(usuarioService.obtenerUsuario(email));
        // Opcional: si se vincula a una recoleccion, el usuario debe ser una de sus partes
        if (crearTicketDTO.getSolicitudId() != null) {
            ticket.setSolicitud(
                    solicitudRecoleccionService.obtenerSolicitudComoParte(
                            email, crearTicketDTO.getSolicitudId()));
        }
        ticket.setCategoria(crearTicketDTO.getCategoria());
        ticket.setAsunto(crearTicketDTO.getAsunto().trim());
        ticket.setDescripcion(crearTicketDTO.getDescripcion());
        return aDTO(ticketSoporteRepositorio.save(ticket));
    }

    @Override
    public List<TicketSoporteDTO> listarMisTickets(String email, String estado) {
        if (estado != null && !ABIERTO.equals(estado) && !CERRADO.equals(estado)) {
            throw new IllegalArgumentException("estado: debe ser abierto o cerrado");
        }
        Usuario usuario = usuarioService.obtenerUsuario(email);
        List<TicketSoporte> tickets =
                estado == null
                        ? ticketSoporteRepositorio.findByUsuario_IdOrderByFechaCreacionDesc(
                                usuario.getId())
                        : ticketSoporteRepositorio
                                .findByUsuario_IdAndEstadoOrderByFechaCreacionDesc(
                                        usuario.getId(), estado);
        return tickets.stream().map(this::aDTO).toList();
    }

    @Transactional
    @Override
    public EvidenciaTicketDTO subirEvidencia(String email, Long id, MultipartFile archivo) {
        TicketSoporte ticket =
                ticketSoporteRepositorio
                        .findById(id)
                        .orElseThrow(() -> new NoSuchElementException("Ticket no encontrado"));
        if (!ticket.getUsuario().getEmail().equals(email)) {
            throw new AccessDeniedException("El ticket no pertenece al usuario");
        }
        String ruta = almacenamientoService.guardar(archivo, CARPETA_EVIDENCIAS, FORMATOS_IMAGEN);
        ticket.setEvidenciaUrl(ruta);
        ticketSoporteRepositorio.save(ticket);
        return new EvidenciaTicketDTO(ruta);
    }

    // Dato de configuracion (application.properties), no consulta la base
    @Override
    public ContactoSoporteDTO obtenerContacto() {
        if (telefonoSoporte.isBlank() || horarioSoporte.isBlank()) {
            throw new RestClientException("Contacto de soporte no configurado");
        }
        return new ContactoSoporteDTO(telefonoSoporte, horarioSoporte);
    }

    // Mapeo manual: solicitudId puede ser null
    private TicketSoporteDTO aDTO(TicketSoporte ticket) {
        Long solicitudId = ticket.getSolicitud() == null ? null : ticket.getSolicitud().getId();
        return new TicketSoporteDTO(
                ticket.getId(),
                solicitudId,
                ticket.getCategoria(),
                ticket.getEstado(),
                ticket.getAsunto(),
                ticket.getDescripcion(),
                ticket.getEvidenciaUrl(),
                ticket.getFechaCreacion(),
                ticket.getFechaCierre());
    }
}
