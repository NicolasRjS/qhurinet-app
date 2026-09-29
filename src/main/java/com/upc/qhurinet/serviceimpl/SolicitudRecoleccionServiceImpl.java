package com.upc.qhurinet.serviceimpl;

import com.upc.qhurinet.repositories.SolicitudRecoleccionRepositorio;
import com.upc.qhurinet.services.SolicitudRecoleccionService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SolicitudRecoleccionServiceImpl implements SolicitudRecoleccionService {
    @Autowired
    private SolicitudRecoleccionRepositorio solicitudRecoleccionRepositorio;
    @Autowired
    private ModelMapper modelMapper;
}
