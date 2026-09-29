package com.upc.qhurinet.serviceimpl;

import com.upc.qhurinet.repositories.RutaRecoleccionRepositorio;
import com.upc.qhurinet.services.RutaRecoleccionService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RutaRecoleccionServiceImpl implements RutaRecoleccionService {
    @Autowired
    private RutaRecoleccionRepositorio rutaRecoleccionRepositorio;
    @Autowired
    private ModelMapper modelMapper;
}
