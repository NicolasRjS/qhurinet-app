package com.upc.qhurinet.repositories;

import com.upc.qhurinet.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepositorio extends JpaRepository<Usuario, Long> {
    // Autenticacion: el email es el identificador de la cuenta y el sujeto del token
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select e from Usuario e where e.id = :id")
    java.util.Optional<Usuario> buscarParaActualizar(@org.springframework.data.repository.query.Param("id") Long id);

    java.util.List<Usuario> findByFotoPerfilUrl(String url);

}
