package com.frh.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
public class OpenApiCustomizationConfig {

    private static final Set<String> COMMON_ERROR_CODES = Set.of("400", "401", "403", "404", "409");

    @Bean
    public OpenApiCustomizer commonResponsesOpenApiCustomizer() {
        return this::customizeOpenApi;
    }

    private void customizeOpenApi(OpenAPI openApi) {
        if (openApi.getPaths() == null) {
            return;
        }

        openApi.getPaths().forEach((path, pathItem) -> {
            if (pathItem == null) {
                return;
            }

            pathItem.readOperationsMap().forEach((httpMethod, operation) -> {
                ApiResponses responses = operation.getResponses();
                if (responses == null) {
                    return;
                }

                COMMON_ERROR_CODES.forEach(code ->
                    responses.computeIfAbsent(code, ignored -> new ApiResponse().description("Client error"))
                );

                patchArrayResponseSchema(path, httpMethod, responses);
            });
        });
    }

    private void patchArrayResponseSchema(String path, PathItem.HttpMethod method, ApiResponses responses) {
        if (responses.get("200") == null) {
            return;
        }

        if ("/api/orders".equals(path) && method == PathItem.HttpMethod.GET) {
            setArraySchema(responses, "200", "#/components/schemas/Order");
        } else if ("/api/orders/store/{storeId}".equals(path) && method == PathItem.HttpMethod.GET) {
            setArraySchema(responses, "200", "#/components/schemas/Order");
        } else if ("/api/orders/store/{storeId}/status/{status}".equals(path)
            && method == PathItem.HttpMethod.GET) {
            setArraySchema(responses, "200", "#/components/schemas/Order");
        } else if ("/api/orders/status/{status}".equals(path) && method == PathItem.HttpMethod.GET) {
            setArraySchema(responses, "200", "#/components/schemas/Order");
        } else if ("/api/analytics/supplier/{supplierId}/top-products".equals(path)
            && method == PathItem.HttpMethod.GET) {
            setArraySchema(responses, "200", "#/components/schemas/TopSellingItemDto");
        }
    }

    private void setArraySchema(ApiResponses responses, String statusCode, String componentRef) {
        ApiResponse response = responses.get(statusCode);
        if (response == null) {
            return;
        }

        ArraySchema arraySchema = new ArraySchema();
        arraySchema.setItems(new Schema<>().$ref(componentRef));

        MediaType mediaType = new MediaType().schema(arraySchema);
        Content content = new Content();
        content.addMediaType("*/*", mediaType);
        response.setContent(content);
    }
}

