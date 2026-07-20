/**
* Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*      http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.opengroup.osdu.crs.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import java.io.IOException;

import java.io.PrintWriter;
import org.springframework.security.core.AuthenticationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.opengroup.osdu.crs.middleware.AuthenticationService;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class UnauthorizedErrorResponseTest {

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
    void shouldReturnUnauthorizedWhenAuthenticationException() throws IOException {
        when(response.getWriter()).thenReturn(writer);
        sut.commence(request, response, authenticationException);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    void shouldReturnUnauthorizedWhenAccessDenied() throws IOException {
        when(response.getWriter()).thenReturn(writer);
        sut.handle(request, response, null);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
}