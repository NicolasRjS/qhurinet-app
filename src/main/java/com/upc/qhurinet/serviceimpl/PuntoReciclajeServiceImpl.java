package com.upc.qhurinet.serviceimpl;

import com.upc.qhurinet.dtos.PuntoReciclajeDTO;
import com.upc.qhurinet.entities.PuntoReciclaje;
import com.upc.qhurinet.repositories.PuntoReciclajeRepositorio;
import com.upc.qhurinet.services.PuntoReciclajeService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
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

        String textoBuscado = q == null || q.isBlank() ? null : q.trim().toLowerCase(Locale.ROOT);
        return puntoReciclajeRepositorio.findAll()
                .stream()
                .filter(PuntoReciclaje::isActivo)
                .filter(punto -> material == null || material.isEmpty()
                        || punto.getMateriales().stream()
                        .anyMatch(categoria -> material.contains(categoria.getId())))
                .filter(punto -> type == null || type.equals(punto.getTipo()))
                .filter(punto -> textoBuscado == null || contiene(punto.getNombre(), textoBuscado)
                        || contiene(punto.getDireccion(), textoBuscado))
                .map(punto -> aDTO(punto))
                .toList();
    }

    private boolean contiene(String valor, String textoBuscado) {
        return valor != null && normalizar(valor).contains(normalizar(textoBuscado));
    }

    @Override
    public PuntoReciclajeDTO buscarPorId(Long id) {
        return aDTO(obtenerPunto(id));
    }

    @Override
    public PuntoReciclaje obtenerPunto(Long id) {
        return puntoReciclajeRepositorio.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Punto de reciclaje no encontrado: " + id));
    }
    private String normalizar(String texto) {
        return Normalizer.normalize(texto.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }

    private PuntoReciclajeDTO aDTO(PuntoReciclaje punto) {
        PuntoReciclajeDTO dto = modelMapper.map(punto, PuntoReciclajeDTO.class);
        if (dto.getCalificacionPromedio() != null && dto.getCalificacionPromedio().signum() == 0) dto.setCalificacionPromedio(null);
        return dto;
    }

}
