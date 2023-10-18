package org.opengroup.osdu.crs.security;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import java.io.IOException;

import java.io.PrintWriter;
import org.springframework.security.core.AuthenticationException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.opengroup.osdu.crs.middleware.AuthenticationService;
import org.mockito.junit.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class UnauthorizedErrorResponseTest {

    @Mock
    AuthenticationService authenticationService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private AuthenticationException authenticationException;
    @InjectMocks
    private AuthSecurityConfig sut;

    @Mock
    private PrintWriter writer;

    @Test
    public void shouldReturnUnauthorizedWhenAuthenticationException() throws IOException {
        when(response.getWriter()).thenReturn(writer);
        sut.commence(request, response, authenticationException);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    public void shouldReturnUnauthorizedWhenAccessDenied() throws IOException {
        when(response.getWriter()).thenReturn(writer);
        sut.handle(request, response, null);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
}