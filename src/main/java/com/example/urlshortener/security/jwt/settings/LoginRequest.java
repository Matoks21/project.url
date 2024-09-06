package com.example.urlshortener.security.jwt.settings;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
@NotBlank
public class LoginRequest {
    String username;
    String password;

}
