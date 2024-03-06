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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.opengroup.osdu.core.common.entitlements.IEntitlementsFactory;
import org.opengroup.osdu.core.common.entitlements.IEntitlementsService;
import org.opengroup.osdu.core.common.model.entitlements.EntitlementsException;
import org.opengroup.osdu.core.common.model.entitlements.Groups;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import org.springframework.web.servlet.HandlerExceptionResolver;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import org.mockito.junit.MockitoJUnitRunner;
import java.util.Enumeration;
import org.opengroup.osdu.core.common.http.HttpResponse;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticationRequestFilterTest {
    @Mock
    HandlerExceptionResolver handlerExceptionResolver;

    @Mock
    HttpServletRequest httpServletRequest;

    @Mock
    HttpServletResponse httpServletResponse;

    @Mock
    IEntitlementsService entitlementsService;

    @Mock
    FilterChain filterChain;

    @Mock
    IEntitlementsFactory entitlementsFactory;

    @Test
    public void should_successfully_constructor() {
        AuthenticationRequestFilter authenticationRequestFilter = new AuthenticationRequestFilter("entitlementUrl",
                handlerExceptionResolver);
        assertNotNull(authenticationRequestFilter);
    } 
    
    @Test
    public void shouldFilteWhenAuthenticated()
            throws ServletException, IOException, EntitlementsException, Exception {
        Enumeration<String> headerNames = Mockito.mock(Enumeration.class);
        Mockito.when(headerNames.hasMoreElements()).thenReturn(false);
        Mockito.when(httpServletRequest.getHeaderNames()).thenReturn(headerNames);

        AuthenticationRequestFilter sut = new AuthenticationRequestFilter("entitlementUrl",
                handlerExceptionResolver);
        AuthenticationRequestFilter sutSpy = Mockito.spy(sut);
                   
        Mockito.when(sutSpy.getEntitlementsFactory()).thenReturn(entitlementsFactory);

        Mockito.when(entitlementsFactory.create(Mockito.any(DpsHeaders.class))).thenReturn(entitlementsService);
        Groups groups = new Groups();
        groups.setMemberEmail("email");
        Mockito.when(entitlementsService.getGroups()).thenReturn(groups);

        sutSpy.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);
        Mockito.verifyNoMoreInteractions(handlerExceptionResolver);
    }
   
    @Test
    public void shouldThrowEntitlementsExceptionWhenUnAuthenticated()
            throws ServletException, IOException, EntitlementsException, Exception {

        Enumeration<String> headerNames = Mockito.mock(Enumeration.class);
        Mockito.when(headerNames.hasMoreElements()).thenReturn(false);
        Mockito.when(httpServletRequest.getHeaderNames()).thenReturn(headerNames);

        AuthenticationRequestFilter sut = new AuthenticationRequestFilter("entitlementUrl",
                handlerExceptionResolver);
        AuthenticationRequestFilter sutSpy = Mockito.spy(sut);

        Mockito.when(sutSpy.getEntitlementsFactory()).thenReturn(entitlementsFactory);
        Mockito.when(entitlementsFactory.create(Mockito.any(DpsHeaders.class))).thenReturn(entitlementsService);
  
        HttpResponse response = new HttpResponse();
        response.setResponseCode(500);
        when(entitlementsService.getGroups()).thenThrow(new EntitlementsException("", response));

        sutSpy.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);
        Mockito.verify(handlerExceptionResolver).resolveException(Mockito.eq(httpServletRequest),
                Mockito.eq(httpServletResponse), Mockito.eq(null), Mockito.any(AppException.class));

    }

    @Test
    public void shouldThrowNullExceptionWhenUnAuthenticated()
            throws ServletException, IOException, EntitlementsException, Exception {

        Enumeration<String> headerNames = Mockito.mock(Enumeration.class);
        Mockito.when(headerNames.hasMoreElements()).thenReturn(false);
        Mockito.when(httpServletRequest.getHeaderNames()).thenReturn(headerNames);


        AuthenticationRequestFilter sut = new AuthenticationRequestFilter("entitlementUrl",
                handlerExceptionResolver);
        AuthenticationRequestFilter sutSpy = Mockito.spy(sut);
    
        Mockito.when(sutSpy.getEntitlementsFactory()).thenReturn(entitlementsFactory);
        Mockito.when(entitlementsFactory.create(Mockito.any(DpsHeaders.class))).thenReturn(entitlementsService);

        when(entitlementsService.getGroups()).thenThrow(new NullPointerException());

        sutSpy.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);
        Mockito.verify(handlerExceptionResolver).resolveException(Mockito.eq(httpServletRequest),
                Mockito.eq(httpServletResponse), Mockito.eq(null), Mockito.any(AppException.class));   

    }     
   
}
