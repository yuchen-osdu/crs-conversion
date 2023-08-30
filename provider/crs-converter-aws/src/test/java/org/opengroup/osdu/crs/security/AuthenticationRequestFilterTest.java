package org.opengroup.osdu.crs.security;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.opengroup.osdu.core.common.entitlements.EntitlementsFactory;
import org.opengroup.osdu.core.common.entitlements.IEntitlementsService;
import org.opengroup.osdu.core.common.model.entitlements.EntitlementsException;
import org.opengroup.osdu.core.common.model.entitlements.Groups;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.powermock.reflect.Whitebox;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import org.powermock.core.classloader.annotations.PrepareForTest;

@RunWith(PowerMockRunner.class)
@PrepareForTest(AuthenticationRequestFilter.class)
public class AuthenticationRequestFilterTest  {

    @Test
    public void should_successfully_constructor() {
        HandlerExceptionResolver handlerExceptionResolver = mock(HandlerExceptionResolver.class);
        AuthenticationRequestFilter authenticationRequestFilter = new AuthenticationRequestFilter("entitlementUrl",
                handlerExceptionResolver);
        assertNotNull(authenticationRequestFilter);
    } 
    
    @Test
    public void shouldThrowExceptionWhenUnAuthenticated()
            throws ServletException, IOException, EntitlementsException, Exception {
        HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = Mockito.mock(HttpServletResponse.class);
        HandlerExceptionResolver handlerExceptionResolver = Mockito.mock(HandlerExceptionResolver.class);

        AuthenticationRequestFilter authenticationRequestFilter = new AuthenticationRequestFilter("entitlementUrl",
                handlerExceptionResolver);

        FilterChain filterChain = Mockito.mock(FilterChain.class);
        Groups groups = Mockito.mock(Groups.class);
        DpsHeaders headers = Mockito.mock(DpsHeaders.class);
        EntitlementsFactory entitlementsFactory = Mockito.mock(EntitlementsFactory.class);

        entitlementsFactory = Whitebox.invokeMethod(authenticationRequestFilter,
                "getEntitlementsFactory");

        IEntitlementsService entitlementsService = Whitebox.invokeMethod(entitlementsFactory,
                "create", headers);

        Exception exception = null;
        try {
            Mockito.when(entitlementsService.getGroups()).thenReturn(groups);
            authenticationRequestFilter.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);
        } catch (Exception ex) {
            exception = ex;
        }
        assertNotNull(exception);
    }

}
