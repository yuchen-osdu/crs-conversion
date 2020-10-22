package org.opengroup.osdu.crs.middleware;

import org.opengroup.osdu.core.common.entitlements.EntitlementsAPIConfig;
import org.opengroup.osdu.core.common.entitlements.EntitlementsFactory;
import org.opengroup.osdu.core.common.entitlements.IEntitlementsFactory;
import org.opengroup.osdu.core.common.entitlements.IEntitlementsService;
import org.opengroup.osdu.core.common.model.entitlements.EntitlementsException;
import org.opengroup.osdu.core.common.model.entitlements.Groups;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.crs.util.AppException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Component
public class AuthenticationRequestFilter extends OncePerRequestFilter {

    private static Logger logger = Logger.getLogger(AuthenticationRequestFilter.class.getName());

    private final String entitlementUrl;

    public AuthenticationRequestFilter(@NonNull @Value("${ENTITLEMENT_URL}") String entitlementUrl) {
        this.entitlementUrl = entitlementUrl;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest httpServletRequest, @NonNull HttpServletResponse httpServletResponse, @NonNull FilterChain filterChain) throws ServletException, IOException {
        MultiValueMap<String, String> requestHeaders = httpHeaders(httpServletRequest);
        DpsHeaders dpsHeaders = DpsHeaders.createFromEntrySet(requestHeaders.entrySet());
        dpsHeaders.addCorrelationIdIfMissing();
        IEntitlementsFactory factory = getEntitlementsFactory();
        IEntitlementsService service = factory.create(dpsHeaders);
        try {
            Groups groups = service.getGroups();
            String message = String.format("User authenticated | User: %s", groups.getMemberEmail());
            logger.fine(message);

            filterChain.doFilter(httpServletRequest, httpServletResponse);
        } catch (EntitlementsException e) {
            String message = String.format(String.format("User not authenticated. Response: %s", e.getHttpResponse()), e);
            logger.warning(message);
            throw AppException.createUnauthorized("Error: " + e.getMessage());
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
        return new EntitlementsFactory(EntitlementsAPIConfig.builder().rootUrl(entitlementUrl).build());
    }
}
