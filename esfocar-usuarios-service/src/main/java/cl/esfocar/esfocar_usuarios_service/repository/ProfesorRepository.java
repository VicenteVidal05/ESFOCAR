package cl.esfocar.esfocar_usuarios_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.esfocar.esfocar_usuarios_service.model.Profesor;

import java.util.Optional;

public interface ProfesorRepository extends JpaRepository<Profesor, String> {
    Optional<Profesor> findByCorreoIgnoreCase(String correo);
    boolean existsByCorreoIgnoreCase(String correo);
}