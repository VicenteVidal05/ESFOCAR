package cl.esfocar.esfocar_rubricas_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "evaluaciones_rubrica")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluacionRubrica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @Column(name = "plantilla_id", nullable = false)
    private Integer plantillaId;

    @NotBlank
    @Column(name = "profesor_correo", nullable = false, length = 150)
    private String profesorCorreo;

    @NotBlank
    @Column(name = "titulo_rubrica", nullable = false, length = 200)
    private String tituloRubrica;

    @NotBlank
    @Column(name = "nombre_estudiante", nullable = false, length = 150)
    private String nombreEstudiante;

    @NotBlank
    @Column(name = "items_json", nullable = false, columnDefinition = "TEXT")
    private String itemsJson;

    @NotNull
    @Column(name = "puntaje_obtenido", nullable = false)
    private Double puntajeObtenido;

    @NotNull
    @Column(name = "puntaje_maximo", nullable = false)
    private Double puntajeMaximo;

    @Builder.Default
    @Column(nullable = false)
    private Double exigencia = 0.60;

    @NotNull
    @Column(name = "nota_final", nullable = false)
    private Double notaFinal;

    @Column(name = "fecha_evaluacion", nullable = false)
    private Long fechaEvaluacion;

    @PrePersist
    public void prePersist() {
        if (fechaEvaluacion == null) fechaEvaluacion = System.currentTimeMillis();
        if (exigencia == null) exigencia = 0.60;
    }
}