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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final String tenantId;
    private final String clientId;
    private final List<String> allowedOrigins;

    public SecurityConfig(
            @Value("${spring.security.oauth2.resourceserver.jwt.tenant-id}") String tenantId,
            @Value("${spring.security.oauth2.resourceserver.jwt.client-id}") String clientId,
            @Value("${cors.allowed-origins}") List<String> allowedOrigins) {
        this.tenantId = tenantId;
        this.clientId = clientId;
        this.allowedOrigins = allowedOrigins;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/error").permitAll()

                        // Lectura pública
                        .requestMatchers(HttpMethod.GET, "/api/v1/productos/**").permitAll()

                        // Escritura protegida
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
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        String jwkSetUri = "https://login.microsoftonline.com/" + tenantId + "/discovery/v2.0/keys";
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();

        OAuth2TokenValidator<Jwt> withTimestamp = new JwtTimestampValidator();

        OAuth2TokenValidator<Jwt> issuerValidator = jwt -> {
            String issuer = jwt.getIssuer() != null ? jwt.getIssuer().toString() : "";
            if (issuer.contains(tenantId)) {
                return OAuth2TokenValidatorResult.success();
            }
            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_issuer", "El emisor del token '" + issuer + "' no es valido.", null)
            );
        };

        OAuth2TokenValidator<Jwt> audienceValidator = jwt -> {
            List<String> audience = jwt.getAudience();
            if (audience != null && !audience.isEmpty()) {
                return OAuth2TokenValidatorResult.success();
            }
            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_audience", "La audiencia del token no coincide.", null)
            );
        };

        jwtDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(withTimestamp, issuerValidator, audienceValidator));

        return jwtDecoder;
    }
}