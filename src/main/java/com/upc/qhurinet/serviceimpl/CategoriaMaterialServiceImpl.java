package com.upc.qhurinet.serviceimpl;

import com.upc.qhurinet.repositories.CategoriaMaterialRepositorio;
import com.upc.qhurinet.services.CategoriaMaterialService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoriaMaterialServiceImpl implements CategoriaMaterialService {
    @Autowired
    private CategoriaMaterialRepositorio categoriaMaterialRepositorio;
    @Autowired
    private ModelMapper modelMapper;
}
