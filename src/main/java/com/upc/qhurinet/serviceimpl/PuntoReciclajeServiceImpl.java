package com.upc.qhurinet.serviceimpl;

import com.upc.qhurinet.repositories.PuntoReciclajeRepositorio;
import com.upc.qhurinet.services.PuntoReciclajeService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PuntoReciclajeServiceImpl implements PuntoReciclajeService {
    @Autowired
    private PuntoReciclajeRepositorio puntoReciclajeRepositorio;
    @Autowired
    private ModelMapper modelMapper;
}
