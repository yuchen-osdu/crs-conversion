/*
  Copyright 2020 Google LLC
  Copyright 2020 EPAM Systems, Inc

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

package org.opengroup.osdu.crs.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.opengroup.osdu.crs.middleware.AuthenticationRequestFilter;
import org.opengroup.osdu.crs.middleware.AuthenticationService;
import org.opengroup.osdu.crs.util.AppError;
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


@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class AuthSecurityConfig extends WebSecurityConfigurerAdapter implements AccessDeniedHandler,
    AuthenticationEntryPoint {

  private AuthenticationRequestFilter authFilter;

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private static final String[] AUTH_WHITELIST = {
      "/",
      "/actuator/**",
      "/_ah/*",
      "/v2/api-docs",
      "/configuration/ui",
      "/swagger-resources/**",
      "/configuration/security",
      "/swagger-ui.html",
      "/info",
      "/webjars/**",
      "/csrf",
      "/api/crs/converter/actuator",
      "/api/crs/converter/actuator/**",
      "/api/crs/converter/actuator/health",
  };

  //AuthenticationRequestFilter is not a recognized bean, so construct it manually
  public AuthSecurityConfig(AuthenticationService authenticationService) {
    authFilter = new AuthenticationRequestFilter(authenticationService);
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.csrf().disable()
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.NEVER)
        .and()
        .authorizeRequests()
        .antMatchers(AUTH_WHITELIST).permitAll()
        .anyRequest().authenticated()
        .and()
        .addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class);
  }

  @Override
  public void configure(WebSecurity web) {
    web.ignoring().antMatchers(AUTH_WHITELIST);
  }

  @Override
  public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
      AccessDeniedException e) throws IOException {
    writeUnauthorizedError(httpServletResponse);
  }

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException authException) throws IOException {
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
