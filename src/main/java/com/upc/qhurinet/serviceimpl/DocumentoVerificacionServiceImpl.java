package com.upc.qhurinet.serviceimpl;

import com.upc.qhurinet.repositories.DocumentoVerificacionRepositorio;
import com.upc.qhurinet.services.DocumentoVerificacionService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DocumentoVerificacionServiceImpl implements DocumentoVerificacionService {
    @Autowired
    private DocumentoVerificacionRepositorio documentoVerificacionRepositorio;
    @Autowired
    private ModelMapper modelMapper;
}
