package cl.esfocar.esfocar_rubricas_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.esfocar.esfocar_rubricas_service.model.EvaluacionRubrica;

import java.util.List;

public interface EvaluacionRubricaRepository extends JpaRepository<EvaluacionRubrica, Integer> {
    List<EvaluacionRubrica> findByProfesorCorreoIgnoreCaseOrderByFechaEvaluacionDesc(String profesorCorreo);
}