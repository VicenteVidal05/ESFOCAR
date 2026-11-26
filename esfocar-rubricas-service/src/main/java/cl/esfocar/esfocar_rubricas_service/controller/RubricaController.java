package cl.esfocar.esfocar_rubricas_service.controller;

import cl.esfocar.esfocar_rubricas_service.model.EvaluacionRubrica;
import cl.esfocar.esfocar_rubricas_service.model.PlantillaRubrica;
import cl.esfocar.esfocar_rubricas_service.repository.EvaluacionRubricaRepository;
import cl.esfocar.esfocar_rubricas_service.repository.PlantillaRubricaRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RubricaController {

    private final PlantillaRubricaRepository plantillaRepo;
    private final EvaluacionRubricaRepository evaluacionRepo;

    // ==================== PLANTILLAS ====================

    @GetMapping("/plantillas")
    public List<PlantillaRubrica> obtenerPlantillas(@RequestParam String correo) {
        return plantillaRepo.findByProfesorCorreoIgnoreCaseOrderByFechaCreacionDesc(correo.trim());
    }

    @PostMapping("/plantillas")
    public ResponseEntity<PlantillaRubrica> crearPlantilla(@Valid @RequestBody PlantillaRubrica plantilla) {
        plantilla.setId(null);
        plantilla.setProfesorCorreo(plantilla.getProfesorCorreo().trim().toLowerCase());
        return ResponseEntity.status(HttpStatus.CREATED).body(plantillaRepo.save(plantilla));
    }

    @PutMapping("/plantillas/{id}")
    public ResponseEntity<PlantillaRubrica> actualizarPlantilla(
            @PathVariable Integer id,
            @Valid @RequestBody PlantillaRubrica datos) {
        return plantillaRepo.findById(id)
                .map(existente -> {
                    existente.setTitulo(datos.getTitulo());
                    existente.setItemsJson(datos.getItemsJson());
                    existente.setPuntajeMaximo(datos.getPuntajeMaximo());
                    existente.setExigencia(datos.getExigencia() != null ? datos.getExigencia() : 0.60);
                    return ResponseEntity.ok(plantillaRepo.save(existente));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/plantillas/{id}")
    public ResponseEntity<Void> eliminarPlantilla(@PathVariable Integer id) {
        if (!plantillaRepo.existsById(id)) return ResponseEntity.notFound().build();
        plantillaRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== EVALUACIONES ====================

    @GetMapping("/evaluaciones")
    public List<EvaluacionRubrica> obtenerEvaluaciones(@RequestParam String correo) {
        return evaluacionRepo.findByProfesorCorreoIgnoreCaseOrderByFechaEvaluacionDesc(correo.trim());
    }

    @PostMapping("/evaluaciones")
    public ResponseEntity<EvaluacionRubrica> crearEvaluacion(@Valid @RequestBody EvaluacionRubrica evaluacion) {
        evaluacion.setId(null);
        evaluacion.setProfesorCorreo(evaluacion.getProfesorCorreo().trim().toLowerCase());
        return ResponseEntity.status(HttpStatus.CREATED).body(evaluacionRepo.save(evaluacion));
    }

    @PutMapping("/evaluaciones/{id}")
    public ResponseEntity<EvaluacionRubrica> actualizarEvaluacion(
            @PathVariable Integer id,
            @Valid @RequestBody EvaluacionRubrica datos) {
        return evaluacionRepo.findById(id)
                .map(existente -> {
                    existente.setNombreEstudiante(datos.getNombreEstudiante());
                    existente.setItemsJson(datos.getItemsJson());
                    existente.setPuntajeObtenido(datos.getPuntajeObtenido());
                    existente.setPuntajeMaximo(datos.getPuntajeMaximo());
                    existente.setNotaFinal(datos.getNotaFinal());
                    existente.setFechaEvaluacion(System.currentTimeMillis());
                    return ResponseEntity.ok(evaluacionRepo.save(existente));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/evaluaciones/{id}")
    public ResponseEntity<Void> eliminarEvaluacion(@PathVariable Integer id) {
        if (!evaluacionRepo.existsById(id)) return ResponseEntity.notFound().build();
        evaluacionRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}