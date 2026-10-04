package com.upc.qhurinet.config;

import com.upc.qhurinet.dtos.PerfilUsuarioDTO;
import com.upc.qhurinet.entities.Usuario;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper
                .typeMap(Usuario.class, PerfilUsuarioDTO.class)
                .addMappings(mapeo -> mapeo.skip(PerfilUsuarioDTO::setMateriales));
        return modelMapper;
    }
}
