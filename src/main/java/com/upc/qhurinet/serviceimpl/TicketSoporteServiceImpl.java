package com.upc.qhurinet.serviceimpl;

import com.upc.qhurinet.repositories.TicketSoporteRepositorio;
import com.upc.qhurinet.services.TicketSoporteService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TicketSoporteServiceImpl implements TicketSoporteService {
    @Autowired
    private TicketSoporteRepositorio ticketSoporteRepositorio;
    @Autowired
    private ModelMapper modelMapper;
}
