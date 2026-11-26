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
@Table(name = "plantillas_rubrica")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlantillaRubrica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank
    @Column(name = "profesor_correo", nullable = false, length = 150)
    private String profesorCorreo;

    @NotBlank
    @Column(nullable = false, length = 200)
    private String titulo;

    @NotBlank
    @Column(name = "items_json", nullable = false, columnDefinition = "TEXT")
    private String itemsJson;

    @NotNull
    @Column(name = "puntaje_maximo", nullable = false)
    private Double puntajeMaximo;

    @Builder.Default
    @Column(nullable = false)
    private Double exigencia = 0.60;

    @Column(name = "fecha_creacion", nullable = false)
    private Long fechaCreacion;

    @PrePersist
    public void prePersist() {
        if (fechaCreacion == null) fechaCreacion = System.currentTimeMillis();
        if (exigencia == null) exigencia = 0.60;
    }
}