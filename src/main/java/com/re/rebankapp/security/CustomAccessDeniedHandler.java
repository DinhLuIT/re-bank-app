package com.re.rebankapp.security;

import com.re.rebankapp.dto.response.ApiResponse;
import com.re.rebankapp.exception.ResponseCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(@NonNull HttpServletRequest request,
                       @NonNull HttpServletResponse response,
                       @NonNull AccessDeniedException accessDeniedException) throws IOException {

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        ApiResponse<Object> body = ApiResponse.error(ResponseCode.FORBIDDEN);

        final ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), body);
    }
}
