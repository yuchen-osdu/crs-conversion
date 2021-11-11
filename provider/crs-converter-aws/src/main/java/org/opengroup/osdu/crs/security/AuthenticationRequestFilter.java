// Copyright © 2020 Amazon Web Services
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
 
package org.opengroup.osdu.crs.security;

import org.opengroup.osdu.core.common.entitlements.EntitlementsAPIConfig;
import org.opengroup.osdu.core.common.entitlements.EntitlementsFactory;
import org.opengroup.osdu.core.common.entitlements.IEntitlementsFactory;
import org.opengroup.osdu.core.common.entitlements.IEntitlementsService;
import org.opengroup.osdu.core.common.http.HttpConfiguration;
import org.opengroup.osdu.core.common.http.json.HttpResponseBodyMapper;
import org.opengroup.osdu.core.common.model.entitlements.EntitlementsException;
import org.opengroup.osdu.core.common.model.entitlements.Groups;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AuthenticationRequestFilter extends OncePerRequestFilter {

    private static Logger logger = Logger.getLogger(AuthenticationRequestFilter.class.getName());

    private final String entitlementsUrl;
    private final HandlerExceptionResolver handlerExceptionResolver;
    private final HttpResponseBodyMapper httpResponseBodyMapper;

    public AuthenticationRequestFilter(@Value("${osdu.entitlement.url}") String entitlementsUrl,
                                       HandlerExceptionResolver handlerExceptionResolver) {
        this.entitlementsUrl = entitlementsUrl;
        this.handlerExceptionResolver = handlerExceptionResolver;
        this.httpResponseBodyMapper = new HttpResponseBodyMapper(new HttpConfiguration().jsonObjectMapper());
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest httpServletRequest,
                                    @NonNull HttpServletResponse httpServletResponse,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        MultiValueMap<String, String> requestHeaders = httpHeaders(httpServletRequest);
        DpsHeaders dpsHeaders = DpsHeaders.createFromEntrySet(requestHeaders.entrySet());
        dpsHeaders.addCorrelationIdIfMissing();
        IEntitlementsFactory factory = getEntitlementsFactory();
        IEntitlementsService service = factory.create(dpsHeaders);
        try {
            Groups groups = service.getGroups();
            String message = String.format("User authenticated | User: %s", groups.getMemberEmail());
            logger.info(message);
            putAuthenticationIntoContext(groups);
            filterChain.doFilter(httpServletRequest, httpServletResponse);
        } catch (EntitlementsException e) {
            String message = String.format(String.format("User not authenticated. Response: %s", e.getHttpResponse()), e);
            logger.warning(message);
            AppException unauthorized =  AppException.createUnauthorized("Error: " + e.getMessage());
            handlerExceptionResolver.resolveException(httpServletRequest, httpServletResponse, null, unauthorized);
        }
        catch (NullPointerException e) { // Common library throws null pointer exception when auth permission is denied.
            String message = String.format("User not authenticated. Null pointer exception: %s", e.getMessage());
            logger.warning(message);
            AppException unauthorized = AppException.createUnauthorized("Error: " + e.getMessage());
            handlerExceptionResolver.resolveException(httpServletRequest, httpServletResponse, null, unauthorized);
        }
    }

    private HttpHeaders httpHeaders(@NonNull HttpServletRequest httpRequest) {
        return Collections
                .list(httpRequest.getHeaderNames())
                .stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        h -> Collections.list(httpRequest.getHeaders(h)),
                        (oldValue, newValue) -> newValue,
                        HttpHeaders::new
                ));
    }

    private IEntitlementsFactory getEntitlementsFactory() {
        return new EntitlementsFactory(EntitlementsAPIConfig.builder().rootUrl(entitlementsUrl).build(), httpResponseBodyMapper);
    }

    private void putAuthenticationIntoContext(Groups groups) {
        AuthenticationToken authentication = new AuthenticationToken(groups, Collections.emptyList());
        authentication.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
