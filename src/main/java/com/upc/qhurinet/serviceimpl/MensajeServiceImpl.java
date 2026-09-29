package com.upc.qhurinet.serviceimpl;

import com.upc.qhurinet.repositories.MensajeRepositorio;
import com.upc.qhurinet.services.MensajeService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MensajeServiceImpl implements MensajeService {
    @Autowired
    private MensajeRepositorio mensajeRepositorio;
    @Autowired
    private ModelMapper modelMapper;
}
