package com.upc.qhurinet.serviceimpl;

import com.upc.qhurinet.dtos.DocumentoVerificacionDTO;
import com.upc.qhurinet.entities.DocumentoVerificacion;
import com.upc.qhurinet.entities.Usuario;
import com.upc.qhurinet.repositories.DocumentoVerificacionRepositorio;
import com.upc.qhurinet.services.AlmacenamientoService;
import com.upc.qhurinet.services.DocumentoVerificacionService;
import com.upc.qhurinet.services.UsuarioService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class DocumentoVerificacionServiceImpl implements DocumentoVerificacionService {
    // Valores admitidos en documentos_verificacion.tipo y .estado
    public static final List<String> TIPOS = List.of("dni", "ruc", "otro");
    public static final String ESTADO_APROBADO = "aprobado";
    public static final String ESTADO_RECHAZADO = "rechazado";

    private static final String CARPETA = "documentos_verificacion";
    private static final List<String> FORMATOS = List.of("jpg", "jpeg", "png", "webp", "pdf"); // US 27-EP4

    @Autowired
    private DocumentoVerificacionRepositorio documentoVerificacionRepositorio;
    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private AlmacenamientoService almacenamientoService;
    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    @Override
    public DocumentoVerificacionDTO subir(String email, String tipo, MultipartFile archivo) {
        if (tipo == null || !TIPOS.contains(tipo)) {
            throw new IllegalArgumentException("tipo: es obligatorio y debe ser uno de " + String.join(", ", TIPOS));
        }
        Usuario usuario = usuarioService.obtenerUsuario(email);
        String ruta = almacenamientoService.guardar(archivo, CARPETA, FORMATOS);

        // Validacion automatica al subir: el estado queda "aprobado" por defecto (US 27-EP4)
        DocumentoVerificacion documento = new DocumentoVerificacion();
        documento.setUsuario(usuario);
        documento.setTipo(tipo);
        documento.setUrlArchivo(ruta);
        return modelMapper.map(documentoVerificacionRepositorio.save(documento), DocumentoVerificacionDTO.class);
    }

    @Override
    public List<DocumentoVerificacionDTO> listarMisDocumentos(String email) {
        Usuario usuario = usuarioService.obtenerUsuario(email);
        // PENDIENTE (query): documentos del usuario ordenados por fecha_subida DESC.
        // Luego: .stream().map(documento -> modelMapper.map(documento, DocumentoVerificacionDTO.class)).toList()
        throw new UnsupportedOperationException("END-11 pendiente: falta la consulta de documentos del usuario " + usuario.getId());
    }
}
