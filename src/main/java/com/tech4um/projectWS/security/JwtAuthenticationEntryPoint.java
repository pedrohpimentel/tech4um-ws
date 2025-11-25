package com.tech4um.projectWS.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import java.io.IOException;

// Esta classe lida com tentativas de acesso a recursos protegidos sem autenticação (token inválido/ausente)
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    // Este método é chamado sempre que um usuário não autenticado tenta acessar um recurso protegido.
    @Override
    public void commence(HttpServletRequest httpServletRequest,
                         HttpServletResponse httpServletResponse,
                         AuthenticationException e) throws IOException, ServletException {

        // Define o status HTTP 401 (Unauthorized)
        httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Acesso negado. Token ausente ou inválido.");
    }
}