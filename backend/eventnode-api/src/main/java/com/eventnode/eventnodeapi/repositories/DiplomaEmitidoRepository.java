package com.eventnode.eventnodeapi.repositories;

import com.eventnode.eventnodeapi.models.DiplomaEmitido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiplomaEmitidoRepository extends JpaRepository<DiplomaEmitido, Integer> {

    List<DiplomaEmitido> findByIdDiploma(Integer idDiploma);

    List<DiplomaEmitido> findByIdUsuario(Integer idUsuario);

    long countByIdDiploma(Integer idDiploma);

    long countByIdDiplomaAndEstadoEnvio(Integer idDiploma, String estadoEnvio);
}
