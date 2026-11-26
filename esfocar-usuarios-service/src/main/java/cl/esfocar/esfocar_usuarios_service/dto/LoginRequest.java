package cl.esfocar.esfocar_usuarios_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank
    private String correo;
    @NotBlank
    private String password;
}