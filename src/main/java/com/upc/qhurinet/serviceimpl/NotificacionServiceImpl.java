package com.upc.qhurinet.serviceimpl;

import com.upc.qhurinet.repositories.NotificacionRepositorio;
import com.upc.qhurinet.services.NotificacionService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificacionServiceImpl implements NotificacionService {
    @Autowired
    private NotificacionRepositorio notificacionRepositorio;
    @Autowired
    private ModelMapper modelMapper;
}
