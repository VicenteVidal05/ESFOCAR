package cl.esfocar.esfocar_rubricas_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.esfocar.esfocar_rubricas_service.model.PlantillaRubrica;

import java.util.List;

public interface PlantillaRubricaRepository extends JpaRepository<PlantillaRubrica, Integer> {
    List<PlantillaRubrica> findByProfesorCorreoIgnoreCaseOrderByFechaCreacionDesc(String profesorCorreo);
}