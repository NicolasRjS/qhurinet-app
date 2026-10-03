package com.upc.qhurinet.controllers;

import com.upc.qhurinet.dtos.*;
import com.upc.qhurinet.services.SolicitudRecoleccionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
 Solicitudes de recoleccion (END-22 a END-31).
 Sin @PreAuthorize: el servicio verifica que el usuario sea una de las dos partes (403 si no).
 Con @PreAuthorize: ademas se exige el rol (reclamar y confirmar = recolector; QR, calificar y seguimiento = generador).
*/
@RestController
@CrossOrigin(origins = "${ip.frontend}", allowCredentials = "true", exposedHeaders = "Authorization")
@RequestMapping("/api/v1/collection-requests")
public class SolicitudRecoleccionController {
    @Autowired
    private SolicitudRecoleccionService solicitudRecoleccionService;

    // END-22: el recolector reclama un anuncio
    @PostMapping
    @PreAuthorize("hasRole('RECOLECTOR')")
    public ResponseEntity<SolicitudDTO> crear(@RequestBody CrearSolicitudDTO crearSolicitudDTO) {
        SolicitudDTO solicitud = solicitudRecoleccionService.crear(emailAutenticado(), crearSolicitudDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(solicitud);
    }

    // END-23: estado = activos | completados | cancelados
    @GetMapping
    public ResponseEntity<List<MiSolicitudDTO>> listarMisSolicitudes(
            @RequestParam(value = "estado", required = false) String estado) {
        return ResponseEntity.ok(solicitudRecoleccionService.listarMisSolicitudes(emailAutenticado(), estado));
    }

    // END-24
    @PatchMapping("/{id}/reschedule")
    public ResponseEntity<SolicitudDTO> reprogramar(@PathVariable Long id, @RequestBody ReprogramarSolicitudDTO reprogramarSolicitudDTO) {
        return ResponseEntity.ok(solicitudRecoleccionService.reprogramar(emailAutenticado(), id, reprogramarSolicitudDTO));
    }

    // END-25
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<SolicitudDTO> cancelar(@PathVariable Long id, @RequestBody CancelarSolicitudDTO cancelarSolicitudDTO) {
        return ResponseEntity.ok(solicitudRecoleccionService.cancelar(emailAutenticado(), id, cancelarSolicitudDTO));
    }

    // END-26
    @PatchMapping("/{id}/priority")
    public ResponseEntity<SolicitudDTO> cambiarPrioridad(@PathVariable Long id, @RequestBody PrioridadSolicitudDTO prioridadSolicitudDTO) {
        return ResponseEntity.ok(solicitudRecoleccionService.cambiarPrioridad(emailAutenticado(), id, prioridadSolicitudDTO));
    }

    // END-27: el generador obtiene el codigo que mostrara al recolector
    @GetMapping("/{id}/qr")
    @PreAuthorize("hasRole('GENERADOR')")
    public ResponseEntity<CodigoQrDTO> obtenerCodigoQr(@PathVariable Long id) {
        return ResponseEntity.ok(solicitudRecoleccionService.obtenerCodigoQr(emailAutenticado(), id));
    }

    // END-28
    @PostMapping("/{id}/validate-qr")
    @PreAuthorize("hasRole('RECOLECTOR')")
    public ResponseEntity<EntregaQrDTO> validarQr(@PathVariable Long id, @RequestBody ValidarQrDTO validarQrDTO) {
        return ResponseEntity.ok(solicitudRecoleccionService.validarQr(emailAutenticado(), id, validarQrDTO));
    }

    // END-29
    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasRole('RECOLECTOR')")
    public ResponseEntity<SolicitudDTO> confirmarEntrega(@PathVariable Long id, @RequestBody ValidarQrDTO datos) {
        return ResponseEntity.ok(solicitudRecoleccionService.confirmarEntrega(emailAutenticado(), id, datos));
    }

    // END-30: el generador califica al recolector
    @PostMapping("/{id}/rating")
    @PreAuthorize("hasRole('GENERADOR')")
    public ResponseEntity<CalificacionDTO> calificar(@PathVariable Long id, @RequestBody CalificarRecolectorDTO calificarRecolectorDTO) {
        return ResponseEntity.ok(solicitudRecoleccionService.calificar(emailAutenticado(), id, calificarRecolectorDTO));
    }

    // END-31
    @GetMapping("/{id}/tracking")
    @PreAuthorize("hasRole('GENERADOR')")
    public ResponseEntity<SeguimientoDTO> obtenerSeguimiento(@PathVariable Long id) {
        return ResponseEntity.ok(solicitudRecoleccionService.obtenerSeguimiento(emailAutenticado(), id));
    }

    private String emailAutenticado() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
    @GetMapping("/{id}")
    public ResponseEntity<DetalleSolicitudDTO> detalle(@PathVariable Long id) {
        return ResponseEntity.ok(solicitudRecoleccionService.obtenerDetalle(emailAutenticado(), id));
    }
    @PatchMapping("/{id}/coordinate")
    public ResponseEntity<SolicitudDTO> coordinar(@PathVariable Long id, @RequestBody ReprogramarSolicitudDTO datos) {
        return ResponseEntity.ok(solicitudRecoleccionService.coordinar(emailAutenticado(), id, datos));
    }
    @PatchMapping("/{id}/start") @PreAuthorize("hasRole('RECOLECTOR')")
    public ResponseEntity<SolicitudDTO> iniciar(@PathVariable Long id) {
        return ResponseEntity.ok(solicitudRecoleccionService.iniciar(emailAutenticado(), id));
    }
    @PutMapping("/{id}/location") @PreAuthorize("hasRole('RECOLECTOR')")
    public ResponseEntity<Void> ubicacion(@PathVariable Long id, @RequestBody UbicacionDTO datos) {
        solicitudRecoleccionService.actualizarUbicacion(emailAutenticado(), id, datos);
        return ResponseEntity.noContent().build();
    }

}
