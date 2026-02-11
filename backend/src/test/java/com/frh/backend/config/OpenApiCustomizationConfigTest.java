package com.frh.backend.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import java.lang.reflect.Method;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springdoc.core.customizers.OpenApiCustomizer;

class OpenApiCustomizationConfigTest {

  private static final Set<String> COMMON_ERROR_CODES = Set.of("400", "401", "403", "404", "409");
  private final OpenApiCustomizationConfig config = new OpenApiCustomizationConfig();

  @Test
  void customizer_handlesNullPaths() {
    OpenApiCustomizer customizer = config.commonResponsesOpenApiCustomizer();
    OpenAPI openApi = new OpenAPI();

    customizer.customise(openApi);

    assertNull(openApi.getPaths());
  }

  @Test
  void customizer_addsCommonResponsesAndArraySchemas() {
    OpenAPI openApi = new OpenAPI().paths(new Paths());

    addOperation(openApi, "/api/orders", PathItem.HttpMethod.GET, true);
    addOperation(openApi, "/api/orders/store/{storeId}", PathItem.HttpMethod.GET, true);
    addOperation(
        openApi, "/api/orders/store/{storeId}/status/{status}", PathItem.HttpMethod.GET, true);
    addOperation(openApi, "/api/orders/status/{status}", PathItem.HttpMethod.GET, true);
    addOperation(
        openApi,
        "/api/analytics/supplier/{supplierId}/top-products",
        PathItem.HttpMethod.GET,
        true);
    addOperation(openApi, "/api/unmapped", PathItem.HttpMethod.GET, true);
    addOperation(openApi, "/api/no-200", PathItem.HttpMethod.GET, false);

    // Exercise false branches of "path + GET" checks.
    addOperation(openApi, "/api/orders", PathItem.HttpMethod.POST, true);
    addOperation(openApi, "/api/orders/store/{storeId}", PathItem.HttpMethod.POST, true);
    addOperation(
        openApi, "/api/orders/store/{storeId}/status/{status}", PathItem.HttpMethod.POST, true);
    addOperation(openApi, "/api/orders/status/{status}", PathItem.HttpMethod.POST, true);
    addOperation(
        openApi,
        "/api/analytics/supplier/{supplierId}/top-products",
        PathItem.HttpMethod.POST,
        true);

    PathItem nullPathItem = null;
    openApi.getPaths().put("/api/null-path-item", nullPathItem);

    PathItem noResponsesPath = new PathItem();
    noResponsesPath.setGet(new Operation());
    openApi.getPaths().addPathItem("/api/no-responses", noResponsesPath);

    OpenApiCustomizer customizer = config.commonResponsesOpenApiCustomizer();
    customizer.customise(openApi);

    assertArrayRef(openApi, "/api/orders", "#/components/schemas/Order");
    assertArrayRef(openApi, "/api/orders/store/{storeId}", "#/components/schemas/Order");
    assertArrayRef(
        openApi, "/api/orders/store/{storeId}/status/{status}", "#/components/schemas/Order");
    assertArrayRef(openApi, "/api/orders/status/{status}", "#/components/schemas/Order");
    assertArrayRef(
        openApi,
        "/api/analytics/supplier/{supplierId}/top-products",
        "#/components/schemas/TopSellingItemDto");
    assertUnpatched200Content(openApi, "/api/orders", PathItem.HttpMethod.POST);
    assertUnpatched200Content(openApi, "/api/orders/store/{storeId}", PathItem.HttpMethod.POST);
    assertUnpatched200Content(
        openApi, "/api/orders/store/{storeId}/status/{status}", PathItem.HttpMethod.POST);
    assertUnpatched200Content(openApi, "/api/orders/status/{status}", PathItem.HttpMethod.POST);
    assertUnpatched200Content(
        openApi, "/api/analytics/supplier/{supplierId}/top-products", PathItem.HttpMethod.POST);

    ApiResponses unmappedResponses =
        openApi.getPaths().get("/api/unmapped").getGet().getResponses();
    assertCommonErrors(unmappedResponses);
    assertNull(unmappedResponses.get("200").getContent());

    ApiResponses no200Responses = openApi.getPaths().get("/api/no-200").getGet().getResponses();
    assertCommonErrors(no200Responses);
    assertNull(no200Responses.get("200"));

    assertNull(openApi.getPaths().get("/api/no-responses").getGet().getResponses());
  }

  @Test
  void setArraySchema_ignoresMissingStatusCode() throws Exception {
    Method method =
        OpenApiCustomizationConfig.class.getDeclaredMethod(
            "setArraySchema", ApiResponses.class, String.class, String.class);
    method.setAccessible(true);

    ApiResponses responses = new ApiResponses();
    method.invoke(config, responses, "200", "#/components/schemas/Order");

    assertNull(responses.get("200"));
  }

  private void addOperation(
      OpenAPI openApi, String path, PathItem.HttpMethod method, boolean include200Response) {
    PathItem pathItem = openApi.getPaths().get(path);
    if (pathItem == null) {
      pathItem = new PathItem();
      openApi.getPaths().addPathItem(path, pathItem);
    }

    Operation operation = new Operation();
    ApiResponses responses = new ApiResponses();
    if (include200Response) {
      responses.addApiResponse("200", new ApiResponse().description("OK"));
    }
    operation.setResponses(responses);
    pathItem.operation(method, operation);
  }

  private void assertCommonErrors(ApiResponses responses) {
    COMMON_ERROR_CODES.forEach(
        code -> {
          assertNotNull(responses.get(code));
          assertEquals("Client error", responses.get(code).getDescription());
        });
  }

  private void assertArrayRef(OpenAPI openApi, String path, String expectedRef) {
    ApiResponses responses = openApi.getPaths().get(path).getGet().getResponses();
    assertCommonErrors(responses);

    ApiResponse response200 = responses.get("200");
    assertNotNull(response200);
    assertNotNull(response200.getContent());

    MediaType mediaType = response200.getContent().get("*/*");
    assertNotNull(mediaType);
    assertTrue(mediaType.getSchema() instanceof ArraySchema);

    ArraySchema schema = (ArraySchema) mediaType.getSchema();
    assertNotNull(schema.getItems());
    assertEquals(expectedRef, schema.getItems().get$ref());
  }

  private void assertUnpatched200Content(OpenAPI openApi, String path, PathItem.HttpMethod method) {
    Operation operation = openApi.getPaths().get(path).readOperationsMap().get(method);
    ApiResponses responses = operation.getResponses();
    assertCommonErrors(responses);
    assertNull(responses.get("200").getContent());
  }
}
