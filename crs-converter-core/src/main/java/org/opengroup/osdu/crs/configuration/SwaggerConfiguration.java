package org.opengroup.osdu.crs.configuration;

import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.Parameter;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Configuration
@EnableSwagger2
public class SwaggerConfiguration {

    private static final String BEARER_AUTH_KEY_NAME = "Bearer Authorization";
    public static final String PASS_AS_HEADER = "header";

    @Bean
    public Docket api(List<Parameter> globalParameters) {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("org.opengroup.osdu.crs.api"))
                .paths(s -> !startsWithIgnoreCase(s, "/error"))
                .build()
                .globalOperationParameters(globalParameters)
                .securitySchemes(singletonList(bearerAuth()))
                .securityContexts(singletonList(securityContext()));
    }

    @Bean
    public Parameter dataPatitionParameter() {
        ParameterBuilder builder = new ParameterBuilder();
        builder.name(DpsHeaders.DATA_PARTITION_ID)
               .description("tenant")
               .defaultValue("opendes")
               .modelRef(new ModelRef("string"))
               .parameterType(PASS_AS_HEADER)
               .required(true)
               .build();
        return builder.build();
    }

    private ApiKey bearerAuth() {
        return new ApiKey(BEARER_AUTH_KEY_NAME, AUTHORIZATION, PASS_AS_HEADER);
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder()
                              .securityReferences(defaultAuth())
                              .forPaths(PathSelectors.any())
                              .build();
    }

    private List<SecurityReference> defaultAuth() {
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[0];
        return singletonList(
                new SecurityReference(BEARER_AUTH_KEY_NAME, authorizationScopes)
        );
    }
}
