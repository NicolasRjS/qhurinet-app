package com.upc.qhurinet.repositories;
import com.upc.qhurinet.entities.IdentidadExterna;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface IdentidadExternaRepositorio extends JpaRepository<IdentidadExterna, Long> {
    Optional<IdentidadExterna> findByProveedorAndIdentificador(String proveedor, String identificador);
}
