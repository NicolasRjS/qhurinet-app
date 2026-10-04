package com.upc.qhurinet.serviceimpl;

import com.upc.qhurinet.services.DisponibilidadService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DisponibilidadServiceImpl implements DisponibilidadService {

    @Value("${disponibilidad.franjas:manana,tarde,noche}")
    private List<String> franjas;

    public void validarFranja(String franja, List<String> errores) {
        if (franja == null || !franjas.contains(franja)) {
            errores.add("franjaHoraria: debe ser una de " + String.join(", ", franjas));
        }
    }
}
