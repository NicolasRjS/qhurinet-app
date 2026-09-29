package com.upc.qhurinet.services;

import com.upc.qhurinet.dtos.DocumentoVerificacionDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentoVerificacionService {
    public DocumentoVerificacionDTO subir(String email, String tipo, MultipartFile archivo);   // END-10
    public List<DocumentoVerificacionDTO> listarMisDocumentos(String email);                  // END-11
}
