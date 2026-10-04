package com.upc.qhurinet.repositories;

import com.upc.qhurinet.entities.Usuario;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepositorio extends JpaRepository<Usuario, Long> {

    // Autenticacion: el email es el identificador de la cuenta y el sujeto del token
    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<Usuario> findByProveedorExternoAndIdExterno(String proveedorExterno, String idExterno);

    List<Usuario> findByPasswordHashIsNull();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Usuario e where e.id = :id")
    Optional<Usuario> buscarParaActualizar(@Param("id") Long id);

    List<Usuario> findByFotoPerfilUrl(String url);
}
