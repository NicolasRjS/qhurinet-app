package com.upc.qhurinet.serviceimpl;

import com.upc.qhurinet.entities.Rol;
import com.upc.qhurinet.repositories.RolRepositorio;
import com.upc.qhurinet.services.RolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RolServiceImpl implements RolService {
    // Valores admitidos en roles.nombre. En @PreAuthorize se usan en mayusculas:
    // hasRole('GENERADOR'), hasRole('RECOLECTOR'), hasRole('ADMINISTRADOR')
    public static final String GENERADOR = "generador";
    public static final String RECOLECTOR = "recolector";
    public static final String ADMINISTRADOR = "administrador";

    // El administrador no se ofrece en el registro publico (US 31-EP4)
    private static final List<String> ROLES_DE_REGISTRO = List.of(GENERADOR, RECOLECTOR);

    @Autowired
    private RolRepositorio rolRepositorio;

    @Override
    public Rol buscarRolDeRegistro(Integer rolId) {
        Rol rol = rolRepositorio.findById(rolId)
                .orElseThrow(() -> new IllegalArgumentException("rolId: el rol " + rolId + " no existe"));
        if (!ROLES_DE_REGISTRO.contains(rol.getNombre())) {
            throw new IllegalArgumentException("rolId: solo se puede registrar como generador o recolector");
        }
        return rol;
    }
}
