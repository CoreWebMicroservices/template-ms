package com.corems.templatems.app.integration;

import com.corems.common.security.CoreMsRoles;
import com.corems.common.security.service.TokenProvider;
import com.corems.templatems.ApiClient;
import com.corems.templatems.api.model.*;
import com.corems.templatems.client.RenderingApi;
import com.corems.templatems.client.TemplatesApi;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class TemplateMsApiIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TokenProvider tokenProvider;
    @Autowired
    private ApiClient apiClient;
    @Autowired
    private TemplatesApi templatesApi;
    @Autowired
    private RenderingApi renderingApi;

    private static final UUID TEST_USER_ID = UUID.randomUUID();
    private static final UUID TEST_ADMIN_ID = UUID.randomUUID();
    private static final String TEST_USER_EMAIL = "testuser@example.com";
    private static final String TEST_ADMIN_EMAIL = "admin@example.com";

    private String createToken(UUID userId, String email, List<String> roles) {
        Map<String, Object> claims = Map.of(
            TokenProvider.CLAIM_EMAIL, email,
            TokenProvider.CLAIM_FIRST_NAME, "Test",
            TokenProvider.CLAIM_LAST_NAME, "User",
            TokenProvider.CLAIM_ROLES, roles
        );
        return tokenProvider.createAccessToken(userId.toString(), claims);
    }

    private void authenticateAsUser() {
        String token = createToken(TEST_USER_ID, TEST_USER_EMAIL, 
            List.of(CoreMsRoles.USER_MS_USER.name()));
        apiClient.setBearerToken(() -> token);
    }

    private void authenticateAsAdmin() {
        String token = createToken(TEST_ADMIN_ID, TEST_ADMIN_EMAIL, 
            List.of(CoreMsRoles.TEMPLATE_MS_ADMIN.name()));
        apiClient.setBearerToken(() -> token);
    }

    @BeforeEach
    void setUp() {
        apiClient.setBasePath("http://localhost:" + port);
        authenticateAsAdmin();
    }

    // ==================== User Access Tests ====================

    @Test
    @Order(1)
    void createTemplate_WhenAuthenticatedAsUser_ShouldBeDenied() {
        authenticateAsUser();

        CreateTemplateRequest request = new CreateTemplateRequest()
                .templateId("test-template")
                .name("Test Template")
                .category(TemplateCategory.EMAIL)
                .content("<h1>Test</h1>");

        assertThatThrownBy(() -> templatesApi.createTemplate(request))
            .isInstanceOf(RestClientResponseException.class)
            .satisfies(ex -> assertThat(((RestClientResponseException) ex).getStatusCode().value()).isIn(401, 403));
    }

    @Test
    @Order(2)
    void updateTemplate_WhenAuthenticatedAsUser_ShouldBeDenied() {
        authenticateAsUser();

        UpdateTemplateRequest request = new UpdateTemplateRequest()
                .name("Updated Template")
                .content("<h1>Updated</h1>");

        assertThatThrownBy(() -> templatesApi.updateTemplate("test-template", request, "en"))
            .isInstanceOf(RestClientResponseException.class)
            .satisfies(ex -> assertThat(((RestClientResponseException) ex).getStatusCode().value()).isIn(401, 403));
    }

    @Test
    @Order(3)
    void deleteTemplate_WhenAuthenticatedAsUser_ShouldBeDenied() {
        authenticateAsUser();

        assertThatThrownBy(() -> templatesApi.deleteTemplate("test-template", "en"))
            .isInstanceOf(RestClientResponseException.class)
            .satisfies(ex -> assertThat(((RestClientResponseException) ex).getStatusCode().value()).isIn(401, 403));
    }

    // ==================== Admin - Template CRUD ====================

    @Test
    @Order(10)
    void createTemplate_WhenValid_ShouldCreateTemplate() {
        CreateTemplateRequest request = new CreateTemplateRequest()
                .templateId("welcome-email")
                .language("en")
                .name("Welcome Email")
                .description("Welcome email for new users")
                .category(TemplateCategory.EMAIL)
                .content("<h1>Welcome, {{user.firstName}}!</h1><p>Thank you for joining {{companyName}}.</p>");

        TemplateResponse response = templatesApi.createTemplate(request);

        assertThat(response).isNotNull();
        assertThat(response.getTemplateId()).isEqualTo("welcome-email");
        assertThat(response.getLanguage()).isEqualTo("en");
        assertThat(response.getName()).isEqualTo("Welcome Email");
        assertThat(response.getCategory()).isEqualTo(TemplateCategory.EMAIL);
    }

    @Test
    @Order(11)
    void createTemplate_WhenDuplicateTemplateId_ShouldReturn409() {
        CreateTemplateRequest request = new CreateTemplateRequest()
                .templateId("welcome-email")
                .language("en")
                .name("Duplicate Template")
                .category(TemplateCategory.EMAIL)
                .content("<h1>Duplicate</h1>");

        assertThatThrownBy(() -> templatesApi.createTemplate(request))
            .isInstanceOf(RestClientResponseException.class)
            .satisfies(ex -> assertThat(((RestClientResponseException) ex).getStatusCode().value()).isEqualTo(409));
    }

    @Test
    @Order(12)
    void getTemplate_WhenExists_ShouldReturnTemplate() {
        TemplateResponse response = templatesApi.getTemplate("welcome-email", "en");

        assertThat(response).isNotNull();
        assertThat(response.getTemplateId()).isEqualTo("welcome-email");
        assertThat(response.getName()).isEqualTo("Welcome Email");
        assertThat(response.getContent()).contains("Welcome");
    }

    @Test
    @Order(13)
    void getTemplate_WhenNotFound_ShouldReturn404() {
        assertThatThrownBy(() -> templatesApi.getTemplate("nonexistent", "en"))
            .isInstanceOf(RestClientResponseException.class)
            .satisfies(ex -> assertThat(((RestClientResponseException) ex).getStatusCode().value()).isEqualTo(404));
    }

    @Test
    @Order(14)
    void updateTemplate_WhenValid_ShouldUpdateTemplate() {
        UpdateTemplateRequest request = new UpdateTemplateRequest()
                .name("Updated Welcome Email")
                .description("Updated description")
                .content("<h1>Welcome, {{user.name}}!</h1>");

        TemplateResponse response = templatesApi.updateTemplate("welcome-email", request, "en");

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Updated Welcome Email");
        assertThat(response.getDescription()).isEqualTo("Updated description");
    }

    @Test
    @Order(15)
    void deleteTemplate_WhenExists_ShouldSoftDelete() {
        SuccessfulResponse response = templatesApi.deleteTemplate("welcome-email", "en");

        assertThat(response.getResult()).isTrue();

        assertThatThrownBy(() -> templatesApi.getTemplate("welcome-email", "en"))
            .isInstanceOf(RestClientResponseException.class)
            .satisfies(ex -> assertThat(((RestClientResponseException) ex).getStatusCode().value()).isEqualTo(404));
    }

    // ==================== Template Listing ====================

    @Test
    @Order(20)
    void listTemplates_ShouldReturnTemplates() {
        CreateTemplateRequest request1 = new CreateTemplateRequest()
                .templateId("invoice-template")
                .name("Invoice Template")
                .category(TemplateCategory.DOCUMENT)
                .content("<h1>Invoice</h1>");
        templatesApi.createTemplate(request1);

        CreateTemplateRequest request2 = new CreateTemplateRequest()
                .templateId("sms-notification")
                .name("SMS Notification")
                .category(TemplateCategory.SMS)
                .content("Your code is {{code}}");
        templatesApi.createTemplate(request2);

        TemplatePagedResponse response = templatesApi.listTemplates(1, 10, null, null, null);

        assertThat(response).isNotNull();
        assertThat(response.getItems()).isNotEmpty();
        assertThat(response.getTotalElements()).isGreaterThanOrEqualTo(2);
    }

    @Test
    @Order(21)
    void listTemplates_WhenFilterByCategory_ShouldReturnFilteredResults() {
        TemplatePagedResponse response = templatesApi.listTemplates(1, 10, null, null, List.of("category:" + TemplateCategory.EMAIL.getValue()));

        assertThat(response).isNotNull();
        assertThat(response.getItems()).allSatisfy(template -> 
            assertThat(template.getCategory()).isEqualTo(TemplateCategory.EMAIL)
        );
    }

    @Test
    @Order(22)
    void listTemplates_ShouldSupportPagination() {
        TemplatePagedResponse page1 = templatesApi.listTemplates(1, 1, null, null, null);
        assertThat(page1.getItems()).hasSize(1);
        assertThat(page1.getPage()).isEqualTo(1);
        assertThat(page1.getPageSize()).isEqualTo(1);

        TemplatePagedResponse page2 = templatesApi.listTemplates(2, 1, null, null, null);
        assertThat(page2.getItems()).hasSize(1);
        assertThat(page2.getPage()).isEqualTo(2);
    }

    // ==================== Template Rendering ====================

    @Test
    @Order(30)
    void renderTemplate_WhenValidParams_ShouldRenderSuccessfully() {
        CreateTemplateRequest createRequest = new CreateTemplateRequest()
                .templateId("render-test")
                .name("Render Test")
                .category(TemplateCategory.EMAIL)
                .content("<h1>Hello, {{name}}!</h1>");
        templatesApi.createTemplate(createRequest);

        authenticateAsUser();

        RenderTemplateRequest renderRequest = new RenderTemplateRequest()
                .params(Map.of("name", "John"));

        RenderTemplateResponse response = renderingApi.renderTemplate("render-test", renderRequest, "en");

        assertThat(response).isNotNull();
        assertThat(response.getHtml()).contains("Hello, John!");
    }

    @Test
    @Order(31)
    void renderTemplate_WhenMissingParams_ShouldReturn400() {
        authenticateAsUser();

        RenderTemplateRequest renderRequest = new RenderTemplateRequest()
                .params(Map.of());

        assertThatThrownBy(() -> renderingApi.renderTemplate("render-test", renderRequest, "en"))
            .isInstanceOf(RestClientResponseException.class)
            .satisfies(ex -> assertThat(((RestClientResponseException) ex).getStatusCode().value()).isEqualTo(400));
    }

    @Test
    @Order(32)
    void renderTemplate_WhenTemplateNotFound_ShouldReturn404() {
        authenticateAsUser();

        RenderTemplateRequest renderRequest = new RenderTemplateRequest()
                .params(Map.of("name", "John"));

        assertThatThrownBy(() -> renderingApi.renderTemplate("nonexistent", renderRequest, "en"))
            .isInstanceOf(RestClientResponseException.class)
            .satisfies(ex -> assertThat(((RestClientResponseException) ex).getStatusCode().value()).isEqualTo(404));
    }

    // ==================== Template Metadata ====================

    @Test
    @Order(40)
    void getTemplateMetadata_WhenExists_ShouldReturnMetadata() {
        authenticateAsUser();

        TemplateMetadataResponse response = renderingApi.getTemplateMetadata("render-test", "en");

        assertThat(response).isNotNull();
        assertThat(response.getTemplateId()).isEqualTo("render-test");
        assertThat(response.getParamSchema()).isNotEmpty();
    }

    @Test
    @Order(41)
    void getTemplateMetadata_WhenNotFound_ShouldReturn404() {
        authenticateAsUser();

        assertThatThrownBy(() -> renderingApi.getTemplateMetadata("nonexistent", "en"))
            .isInstanceOf(RestClientResponseException.class)
            .satisfies(ex -> assertThat(((RestClientResponseException) ex).getStatusCode().value()).isEqualTo(404));
    }

    // ==================== Multi-language Support ====================

    @Test
    @Order(50)
    void createTemplate_WithDifferentLanguages_ShouldCreateSeparateTemplates() {
        CreateTemplateRequest enRequest = new CreateTemplateRequest()
                .templateId("multilang-test")
                .language("en")
                .name("English Template")
                .category(TemplateCategory.EMAIL)
                .content("<h1>Hello</h1>");
        templatesApi.createTemplate(enRequest);

        CreateTemplateRequest esRequest = new CreateTemplateRequest()
                .templateId("multilang-test")
                .language("es")
                .name("Spanish Template")
                .category(TemplateCategory.EMAIL)
                .content("<h1>Hola</h1>");
        templatesApi.createTemplate(esRequest);

        TemplateResponse enResponse = templatesApi.getTemplate("multilang-test", "en");
        TemplateResponse esResponse = templatesApi.getTemplate("multilang-test", "es");

        assertThat(enResponse.getContent()).contains("Hello");
        assertThat(esResponse.getContent()).contains("Hola");
    }

    // ==================== Validation Tests ====================

    @Test
    @Order(60)
    void createTemplate_WhenMissingRequiredFields_ShouldReturn400() {
        CreateTemplateRequest request = new CreateTemplateRequest();

        assertThatThrownBy(() -> templatesApi.createTemplate(request))
            .isInstanceOf(RestClientResponseException.class)
            .satisfies(ex -> assertThat(((RestClientResponseException) ex).getStatusCode().value()).isEqualTo(400));
    }

    @Test
    @Order(61)
    void createTemplate_WhenInvalidHandlebarsSyntax_ShouldReturn400() {
        CreateTemplateRequest request = new CreateTemplateRequest()
                .templateId("invalid-syntax")
                .name("Invalid Syntax")
                .category(TemplateCategory.EMAIL)
                .content("<h1>{{#if unclosed</h1>");

        assertThatThrownBy(() -> templatesApi.createTemplate(request))
            .isInstanceOf(RestClientResponseException.class)
            .satisfies(ex -> assertThat(((RestClientResponseException) ex).getStatusCode().value()).isEqualTo(400));
    }

    // ==================== Unauthorized Access Tests ====================

    @Test
    @Order(70)
    void apiCalls_WhenNotAuthenticated_ShouldReturn401() {
        // Create a fresh API client without any authentication
        ApiClient unauthenticatedClient = new ApiClient();
        unauthenticatedClient.setBasePath("http://localhost:" + port);
        TemplatesApi unauthenticatedTemplatesApi = new TemplatesApi(unauthenticatedClient);

        assertThatThrownBy(() -> unauthenticatedTemplatesApi.listTemplates(1, 10, null, null, null))
            .isInstanceOf(RestClientResponseException.class)
            .satisfies(ex -> assertThat(((RestClientResponseException) ex).getStatusCode().value()).isEqualTo(401));

        assertThatThrownBy(() -> unauthenticatedTemplatesApi.getTemplate("test", "en"))
            .isInstanceOf(RestClientResponseException.class)
            .satisfies(ex -> assertThat(((RestClientResponseException) ex).getStatusCode().value()).isEqualTo(401));
    }
}
