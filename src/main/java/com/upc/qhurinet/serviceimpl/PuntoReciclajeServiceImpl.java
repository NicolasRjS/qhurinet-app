package com.upc.qhurinet.serviceimpl;

import com.upc.qhurinet.dtos.PuntoReciclajeDTO;
import com.upc.qhurinet.entities.PuntoReciclaje;
import com.upc.qhurinet.repositories.PuntoReciclajeRepositorio;
import com.upc.qhurinet.services.PuntoReciclajeService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class PuntoReciclajeServiceImpl implements PuntoReciclajeService {
    // Valores admitidos en puntos_reciclaje.tipo
    public static final List<String> TIPOS = List.of("acopio", "bodega", "reciclador", "municipal");

    @Autowired
    private PuntoReciclajeRepositorio puntoReciclajeRepositorio;
    @Autowired
    private ModelMapper modelMapper;

    @Override
    public List<PuntoReciclajeDTO> listar(List<Integer> material, String type, String q) {
        if (type != null && !TIPOS.contains(type)) {
            throw new IllegalArgumentException("type: debe ser uno de " + String.join(", ", TIPOS));
        }
        // PENDIENTE (query): puntos que aceptan alguno de los materiales, del tipo indicado y cuyo nombre o
        // direccion contengan q (END-34). La distancia requiere la ubicacion del usuario, que END-34 no recibe.
        throw new UnsupportedOperationException("END-34 pendiente: falta la consulta de puntos de reciclaje");
    }

    @Override
    public PuntoReciclajeDTO buscarPorId(Long id) {
        return modelMapper.map(obtenerPunto(id), PuntoReciclajeDTO.class);
    }

    @Override
    public PuntoReciclaje obtenerPunto(Long id) {
        return puntoReciclajeRepositorio.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Punto de reciclaje no encontrado: " + id));
    }
}
