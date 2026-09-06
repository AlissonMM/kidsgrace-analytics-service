package edu.meialua.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Protege /analytics/** exigindo o mesmo JWT emitido pela loja-brinquedos-api,
 * com a claim "roles" contendo "ROLE_ADMIN" — este serviço nunca emite token,
 * só valida o que já vem pronto do login na API principal. Qualquer outro
 * path (ex.: /q/swagger-ui) continua livre.
 */
@Provider
@PreMatching
@Priority(Priorities.AUTHENTICATION)
public class AdminAuthFilter implements ContainerRequestFilter {

    private static final SecretKey SIGNING_KEY = Keys.hmacShaKeyFor(
            JwtSecurityConstants.JWT_SECRET.getBytes(StandardCharsets.UTF_8)
    );

    @Override
    public void filter(ContainerRequestContext requestContext) {
        // getPath() ora vem com barra inicial, ora não, dependendo da
        // implementação JAX-RS — normaliza antes de comparar.
        String path = requestContext.getUriInfo().getPath();
        String normalizedPath = path.startsWith("/") ? path.substring(1) : path;

        if (!normalizedPath.startsWith("analytics") || "OPTIONS".equalsIgnoreCase(requestContext.getMethod())) {
            return;
        }

        String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            abort(requestContext, 401, "Token de autenticação ausente.");
            return;
        }

        String token = authHeader.substring("Bearer ".length());

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(SIGNING_KEY)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            if (!hasAdminRole(claims)) {
                abort(requestContext, 403, "Acesso restrito a administradores.");
            }
        } catch (JwtException | IllegalArgumentException e) {
            abort(requestContext, 401, "Token inválido ou expirado.");
        }
    }

    private boolean hasAdminRole(Claims claims) {
        Object rolesClaim = claims.get("roles");
        if (rolesClaim instanceof List<?> roles) {
            return roles.stream().anyMatch(role -> "ROLE_ADMIN".equals(String.valueOf(role)));
        }
        return false;
    }

    private void abort(ContainerRequestContext requestContext, int status, String message) {
        requestContext.abortWith(
                Response.status(status)
                        .entity("{\"message\":\"" + message + "\"}")
                        .type(MediaType.APPLICATION_JSON)
                        .build()
        );
    }
}
