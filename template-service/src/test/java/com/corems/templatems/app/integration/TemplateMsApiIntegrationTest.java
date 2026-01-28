package com.corems.templatems.app.integration;

import com.corems.common.security.CoreMsRoles;
import com.corems.common.security.service.TokenProvider;
import com.corems.templatems.ApiClient;
import com.corems.templatems.api.model.*;
import com.corems.templatems.client.TemplateManagementApi;
import com.corems.templatems.client.TemplateRenderingApi;
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
    private TemplateManagementApi templateManagementApi;
    @Autowired
    private TemplateRenderingApi templateRenderingApi;

    private static final UUID TEST_ADMIN_ID = UUID.randomUUID();
    private static final String TEST_ADMIN_EMAIL = "admin@example.com";
    
    private static UUID welcomeEmailTemplateId;

    private String createToken(UUID userId, String email, List<String> roles) {
        Map<String, Object> claims = Map.of(
            TokenProvider.CLAIM_EMAIL, email,
            TokenProvider.CLAIM_FIRST_NAME, "Test",
            TokenProvider.CLAIM_LAST_NAME, "User",
            TokenProvider.CLAIM_ROLES, roles
        );
        return tokenProvider.createAccessToken(userId.toString(), claims);
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

    @Test
    @Order(1)
    void createTemplate_WhenValid_ShouldCreateTemplate() {
        CreateTemplateRequest request = new CreateTemplateRequest()
                .templateId("welcome-email")
                .language("en")
                .name("Welcome Email")
                .description("Welcome email for new users")
                .category(TemplateCategory.EMAIL)
                .content("<h1>Welcome, {{user.firstName}}!</h1>");

        TemplateResponse response = templateManagementApi.createTemplate(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getTemplateId()).isEqualTo("welcome-email");
        assertThat(response.getLanguage()).isEqualTo("en");
        assertThat(response.getName()).isEqualTo("Welcome Email");
        
        welcomeEmailTemplateId = response.getId();
    }

    @Test
    @Order(2)
    void getTemplate_WhenExists_ShouldReturnTemplate() {
        TemplateResponse response = templateManagementApi.getTemplate(welcomeEmailTemplateId);

        assertThat(response).isNotNull();
        assertThat(response.getTemplateId()).isEqualTo("welcome-email");
        assertThat(response.getName()).isEqualTo("Welcome Email");
    }

    @Test
    @Order(3)
    void updateTemplate_WhenValid_ShouldUpdateTemplate() {
        UpdateTemplateRequest request = new UpdateTemplateRequest()
                .name("Updated Welcome Email")
                .content("<h1>Welcome, {{user.name}}!</h1>");

        TemplateResponse response = templateManagementApi.updateTemplate(welcomeEmailTemplateId, request);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Updated Welcome Email");
    }

    @Test
    @Order(4)
    void renderTemplate_WhenValidParams_ShouldRenderSuccessfully() {
        RenderTemplateRequest renderRequest = new RenderTemplateRequest()
                .params(Map.of("user", Map.of("name", "John")));

        RenderTemplateResponse response = templateRenderingApi.renderTemplate("welcome-email", renderRequest, "en");

        assertThat(response).isNotNull();
        assertThat(response.getHtml()).contains("Welcome");
    }

    @Test
    @Order(5)
    void getTemplateMetadata_WhenExists_ShouldReturnMetadata() {
        TemplateMetadataResponse response = templateRenderingApi.getTemplateMetadata("welcome-email", "en");

        assertThat(response).isNotNull();
        assertThat(response.getTemplateId()).isEqualTo("welcome-email");
    }

    @Test
    @Order(6)
    void deleteTemplate_WhenExists_ShouldSoftDelete() {
        templateManagementApi.deleteTemplate(welcomeEmailTemplateId);

        assertThatThrownBy(() -> templateManagementApi.getTemplate(welcomeEmailTemplateId))
            .isInstanceOf(RestClientResponseException.class)
            .satisfies(ex -> assertThat(((RestClientResponseException) ex).getStatusCode().value()).isEqualTo(404));
    }
}
