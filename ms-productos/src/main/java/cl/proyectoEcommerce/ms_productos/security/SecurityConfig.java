package cl.proyectoEcommerce.ms_productos.security;

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
                // Desactivado en el microservicio: El API Gateway gestiona CORS completamente
                .cors(cors -> cors.disable())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Peticiones Preflight de CORS
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Swagger y endpoints de sistema
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/error").permitAll()

                        // Endpoints Públicos (Lectura)
                        .requestMatchers(HttpMethod.GET, "/api/v1/productos/**").permitAll()

                        // Endpoints Protegidos (Escritura / Modificación)
                        .requestMatchers(HttpMethod.POST, "/api/v1/productos/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/v1/productos/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/productos/**").authenticated()

                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt -> jwt.decoder(jwtDecoder()))
                );

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        // Endpoint v2.0 de Azure AD para la recuperación de llaves JWKS (con caché LRU interna por Nimbus)
        String jwkSetUri = "https://login.microsoftonline.com/" + tenantId + "/discovery/v2.0/keys";
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();

        // 1. Validador de expiración y vigencia temporal
        OAuth2TokenValidator<Jwt> withTimestamp = new JwtTimestampValidator();

        // 2. Validador flexible de Issuer (soporta v2.0 y sts.windows.net)
        OAuth2TokenValidator<Jwt> issuerValidator = jwt -> {
            String issuer = jwt.getIssuer() != null ? jwt.getIssuer().toString() : "";
            if (issuer.equals("https://login.microsoftonline.com/" + tenantId + "/v2.0") ||
                    issuer.equals("https://sts.windows.net/" + tenantId + "/")) {
                return OAuth2TokenValidatorResult.success();
            }
            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_issuer", "El emisor del token '" + issuer + "' no es valido.", null)
            );
        };

        // 3. Validador estricto de Audiencia (verifica que pertenezca a la aplicación cliente registrada)
        OAuth2TokenValidator<Jwt> audienceValidator = jwt -> {
            List<String> audience = jwt.getAudience();
            if (audience != null && (audience.contains(clientId) || audience.contains("api://" + clientId))) {
                return OAuth2TokenValidatorResult.success();
            }
            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_audience", "La audiencia del token no coincide con este microservicio.", null)
            );
        };

        // Enlace de los validadores
        jwtDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(withTimestamp, issuerValidator, audienceValidator));

        return jwtDecoder;
    }
}
