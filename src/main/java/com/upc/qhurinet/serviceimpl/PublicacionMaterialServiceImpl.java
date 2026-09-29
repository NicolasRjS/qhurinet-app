package com.upc.qhurinet.serviceimpl;

import com.upc.qhurinet.repositories.PublicacionMaterialRepositorio;
import com.upc.qhurinet.services.PublicacionMaterialService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PublicacionMaterialServiceImpl implements PublicacionMaterialService {
    @Autowired
    private PublicacionMaterialRepositorio publicacionMaterialRepositorio;
    @Autowired
    private ModelMapper modelMapper;
}
