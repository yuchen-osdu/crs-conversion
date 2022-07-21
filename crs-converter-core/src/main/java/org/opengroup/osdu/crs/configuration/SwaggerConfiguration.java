package org.opengroup.osdu.crs.configuration;

import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.RequestParameterBuilder;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.ParameterType;
import springfox.documentation.service.RequestParameter;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Configuration
@EnableOpenApi
public class SwaggerConfiguration {

	private static final String BEARER_AUTH_KEY_NAME = "Bearer Authorization";
    public static final String PASS_AS_HEADER = "header";
    public static final String DEFAULT_INCLUDE_PATTERN = "/.*";

    @Bean
    public Docket api() {
    	RequestParameterBuilder builder = new RequestParameterBuilder();
    	List<RequestParameter> parameters = new ArrayList<>();
    	builder.name(DpsHeaders.DATA_PARTITION_ID)
        .description("tenant")
        .in(ParameterType.HEADER)
        .required(true)
        .build();
       parameters.add(builder.build());
    	return new Docket(DocumentationType.OAS_30)
    	    	.globalRequestParameters(parameters)
                .select()
                .apis(RequestHandlerSelectors.basePackage("org.opengroup.osdu.crs.api"))
                .paths(s -> !startsWithIgnoreCase(s, "/error"))
                .build()
                .securitySchemes(singletonList(bearerAuth()))
                .securityContexts(singletonList(securityContext()));
    }


    private ApiKey bearerAuth() {
        return new ApiKey(BEARER_AUTH_KEY_NAME, AUTHORIZATION, PASS_AS_HEADER);
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder()
                              .securityReferences(defaultAuth())
                              .operationSelector(o -> PathSelectors.regex(DEFAULT_INCLUDE_PATTERN).test(o.requestMappingPattern()))
                              .build();
    }

    private List<SecurityReference> defaultAuth() {
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[0];
        return singletonList(
                new SecurityReference(BEARER_AUTH_KEY_NAME, authorizationScopes)
        );
    }
}
