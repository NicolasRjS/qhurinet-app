package com.upc.qhurinet.services;

import com.upc.qhurinet.entities.Rol;

public interface RolService {
    // Devuelve el rol si puede elegirse en el registro publico (generador o recolector)
    public Rol buscarRolDeRegistro(Integer rolId);
}
