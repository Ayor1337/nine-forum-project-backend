package com.ayor.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI forumOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Nine Forum API")
                        .description("Nine Forum 后端接口文档")
                        .version("v1.0.0"))
                .servers(java.util.List.of(new Server().url("/")))
                .externalDocs(new ExternalDocumentation()
                        .description("Knife4j")
                        .url("https://doc.xiaominfo.com/"));
    }

    @Bean
    public GroupedOpenApi forumGroupedOpenApi() {
        return GroupedOpenApi.builder()
                .group("default")
                .packagesToScan("com.ayor.controller")
                .pathsToMatch("/**")
                .build();
    }
}
