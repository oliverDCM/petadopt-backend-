package com.petadopt.infrastructure.adapter.in.web.security;

import com.petadopt.application.port.out.TokenServicePort;
import com.petadopt.application.port.out.TokenServicePort.DatosToken;
import com.petadopt.application.port.out.UsuarioRepositoryPort;
import com.petadopt.domain.model.Usuario;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Lee la cabecera "Authorization: Bearer <token>". El token solo se acepta si la firma es válida, no venció,
 * la cuenta sigue existiendo y la contraseña no cambió desde que se emitió (huella de credenciales).
 * El rol se toma de la base de datos, no del token. Los permisos por rol y por dueño los deciden los casos de uso.
 * Si no hay token o no es válido, no autentica: la cadena de seguridad responde 401.
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String PREFIJO = "Bearer ";

    private final TokenServicePort tokenService;
    private final UsuarioRepositoryPort usuarioRepository;

    public JwtAuthFilter(TokenServicePort tokenService, UsuarioRepositoryPort usuarioRepository) {
        this.tokenService = tokenService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String cabecera = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (cabecera != null && cabecera.startsWith(PREFIJO)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = cabecera.substring(PREFIJO.length()).trim();
            tokenService.validar(token).flatMap(this::usuarioVigente).ifPresent(usuario -> {
                UsuarioAutenticado principal = new UsuarioAutenticado(usuario.id(), usuario.email(), usuario.rol());
                UsernamePasswordAuthenticationToken autenticacion =
                        new UsernamePasswordAuthenticationToken(principal, null, List.of());
                autenticacion.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(autenticacion);
            });
        }
        chain.doFilter(request, response);
    }

    /** La cuenta debe existir y conservar la contraseña con la que se emitió el token. */
    private Optional<Usuario> usuarioVigente(DatosToken datos) {
        return usuarioRepository.buscarPorId(datos.usuarioId())
                .filter(u -> tokenService.huellaCredencial(u).equals(datos.huellaCredencial()));
    }
}
