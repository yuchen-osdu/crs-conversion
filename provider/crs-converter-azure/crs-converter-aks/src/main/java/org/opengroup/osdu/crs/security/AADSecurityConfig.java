package org.opengroup.osdu.crs.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.spring.autoconfigure.aad.AADAppRoleStatelessAuthenticationFilter;
import org.opengroup.osdu.crs.util.AppError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class AADSecurityConfig extends WebSecurityConfigurerAdapter implements AccessDeniedHandler, AuthenticationEntryPoint {

    @Autowired
    private AADAppRoleStatelessAuthenticationFilter appRoleAuthFilter;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String[] AUTH_WHITELIST = {
            "/",
            "/_ah/*",
            "/v2/api-docs",
            "/configuration/ui",
            "/swagger-resources/**",
            "/configuration/security",
            "/swagger-ui.html",
            "/webjars/**",
            "/csrf"
    };

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.NEVER)
                .and()
                .authorizeRequests()
                .antMatchers("/", "/index.html",
                        "/v2/api-docs",
                        "/configuration/ui",
                        "/swagger-resources/**",
                        "/configuration/security",
                        "/swagger",
                        "/swagger-ui.html",
                        "/webjars/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .addFilterBefore(appRoleAuthFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers(AUTH_WHITELIST);
    }

    @Override
    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AccessDeniedException e) throws IOException, ServletException {
        writeUnauthorizedError(httpServletResponse);
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        writeUnauthorizedError(response);
    }

    private static void writeUnauthorizedError(HttpServletResponse response) throws IOException {
        AppError appError = AppError.builder()
                .code(HttpStatus.UNAUTHORIZED.value())
                .message("The user is not authorized to perform this action")
                .reason("Unauthorized")
                .build();
        String body = OBJECT_MAPPER.writeValueAsString(appError);

        PrintWriter out = response.getWriter();
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        out.print(body);
        out.flush();
    }
}
