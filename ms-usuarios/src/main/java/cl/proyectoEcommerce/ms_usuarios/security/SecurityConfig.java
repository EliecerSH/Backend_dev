package cl.proyectoEcommerce.ms_usuarios.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final String tenantId;
    private final String clientId;

    public SecurityConfig(
            @Value("${spring.security.oauth2.resourceserver.jwt.tenant-id}") String tenantId,
            @Value("${spring.security.oauth2.resourceserver.jwt.client-id}") String clientId) {
        this.tenantId = tenantId;
        this.clientId = clientId;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Desactivado en el microservicio: El API Gateway maneja las cabeceras CORS
                .cors(cors -> cors.disable())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Peticiones de verificación previas (Preflight)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Endpoints públicos y documentación Swagger
                        .requestMatchers("/api/v1/public/**", "/swagger-ui/**", "/v3/api-docs/**", "/error").permitAll()
                        // Todo lo demás requiere un JWT válido de Azure
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> 
                        oauth2.jwt(jwt -> jwt.decoder(jwtDecoder()))
                );

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        // Endpoint global v2.0 de llaves JWKS de Azure AD (Nimbus maneja automáticamente el caché LRU)
        String jwkSetUri = "https://login.microsoftonline.com/" + tenantId + "/discovery/v2.0/keys";
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();

        // 1. Validador de tiempo (expiración y tiempo activo)
        OAuth2TokenValidator<Jwt> withTimestamp = new JwtTimestampValidator();

        // 2. Validador flexible de Issuer (Acepta endpoints v2.0 y v1.0 sts.windows.net)
        OAuth2TokenValidator<Jwt> issuerValidator = jwt -> {
            String issuer = jwt.getIssuer() != null ? jwt.getIssuer().toString() : "";
            if (issuer.equals("https://login.microsoftonline.com/" + tenantId + "/v2.0") ||
                    issuer.equals("https://sts.windows.net/" + tenantId + "/")) {
                return OAuth2TokenValidatorResult.success();
            }
            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_issuer", "El emisor " + issuer + " no es valido", null)
            );
        };

        // 3. Validador de Audiencia
        OAuth2TokenValidator<Jwt> audienceValidator = jwt -> {
            List<String> audience = jwt.getAudience();
            if (audience != null && (audience.contains(clientId) || audience.contains("api://" + clientId))) {
                return OAuth2TokenValidatorResult.success();
            }
            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_audience", "La audiencia no coincide con esta API", null)
            );
        };

        // Asignación de validadores en cadena
        jwtDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(withTimestamp, issuerValidator, audienceValidator));

        return jwtDecoder;
    }
}
