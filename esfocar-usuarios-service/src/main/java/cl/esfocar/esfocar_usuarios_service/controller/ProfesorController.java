package cl.esfocar.esfocar_usuarios_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import cl.esfocar.esfocar_usuarios_service.dto.LoginRequest;
import cl.esfocar.esfocar_usuarios_service.model.Profesor;
import cl.esfocar.esfocar_usuarios_service.repository.ProfesorRepository;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/profesores")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProfesorController {

    private final ProfesorRepository profesorRepository;

    @PostMapping("/registro")
    public ResponseEntity<?> registrar(@Valid @RequestBody Profesor profesor) {
        String correoNormalizado = profesor.getCorreo().trim().toLowerCase();
        if (profesorRepository.existsByCorreoIgnoreCase(correoNormalizado)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "El correo ya está registrado"));
        }
        profesor.setCorreo(correoNormalizado);
        profesor.setNombre(profesor.getNombre().trim());
        profesor.setApellidos(profesor.getApellidos().trim());

        Profesor guardado = profesorRepository.save(profesor);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        String correoNormalizado = request.getCorreo().trim().toLowerCase();
        return profesorRepository.findByCorreoIgnoreCase(correoNormalizado)
                .filter(p -> p.getPassword().equals(request.getPassword()))
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Correo o contraseña incorrectos")));
    }

    @GetMapping
    public List<Profesor> listarTodos() {
        return profesorRepository.findAll();
    }
}