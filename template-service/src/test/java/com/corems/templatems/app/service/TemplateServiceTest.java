package com.corems.templatems.app.service;

import com.corems.common.exception.ServiceException;
import com.corems.common.security.SecurityUtils;
import com.corems.common.security.UserPrincipal;
import com.corems.templatems.api.model.*;
import com.corems.templatems.app.entity.TemplateEntity;
import com.corems.templatems.app.exception.TemplateServiceExceptionReasonCodes;
import com.corems.templatems.app.repository.TemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TemplateServiceTest {

    @Mock
    private TemplateRepository templateRepository;

    @Mock
    private RenderingEngine renderingEngine;

    @Mock
    private TemplateValidator templateValidator;

    @InjectMocks
    private TemplateService templateService;

    private TemplateEntity testTemplate;
    private CreateTemplateRequest createRequest;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        
        Map<String, Object> paramSchema = new HashMap<>();
        Map<String, Object> userParam = new HashMap<>();
        userParam.put("required", true);
        userParam.put("type", "string");
        paramSchema.put("user", userParam);

        testTemplate = TemplateEntity.builder()
                .id(1L)
                .uuid(UUID.randomUUID())
                .templateId("welcome-email")
                .language("en")
                .name("Welcome Email")
                .description("Welcome email for new users")
                .category(TemplateCategory.EMAIL)
                .content("<h1>Welcome, {{user.firstName}}!</h1>")
                .paramSchema(paramSchema)
                .isDeleted(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .createdBy(testUserId)
                .updatedBy(testUserId)
                .build();

        createRequest = new CreateTemplateRequest()
                .templateId("welcome-email")
                .language("en")
                .name("Welcome Email")
                .description("Welcome email for new users")
                .category(TemplateCategory.EMAIL)
                .content("<h1>Welcome, {{user.firstName}}!</h1>");
        
        ReflectionTestUtils.setField(templateService, "defaultLanguage", "en");
    }

    @Test
    void createTemplate_WhenValid_ShouldCreateTemplate() {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            UserPrincipal mockPrincipal = mock(UserPrincipal.class);
            when(mockPrincipal.getUserId()).thenReturn(testUserId);
            securityUtils.when(SecurityUtils::getUserPrincipal).thenReturn(mockPrincipal);

            when(templateRepository.findByTemplateIdAndLanguageAndIsDeletedFalse("welcome-email", "en"))
                    .thenReturn(Optional.empty());
            
            Map<String, Object> extractedParams = new HashMap<>();
            Map<String, Object> userParam = new HashMap<>();
            userParam.put("required", false);
            userParam.put("type", "string");
            extractedParams.put("user", userParam);
            
            when(templateValidator.extractParameters(anyString()))
                    .thenReturn(extractedParams);
            when(templateRepository.save(any(TemplateEntity.class)))
                    .thenReturn(testTemplate);

            TemplateResponse response = templateService.createTemplate(createRequest);

            assertThat(response).isNotNull();
            assertThat(response.getTemplateId()).isEqualTo("welcome-email");
            assertThat(response.getName()).isEqualTo("Welcome Email");
            verify(templateValidator).validateSyntax(createRequest.getContent());
            verify(templateRepository).save(any(TemplateEntity.class));
        }
    }

    @Test
    void createTemplate_WhenTemplateExists_ShouldThrowException() {
        when(templateRepository.findByTemplateIdAndLanguageAndIsDeletedFalse("welcome-email", "en"))
                .thenReturn(Optional.of(testTemplate));

        assertThatThrownBy(() -> templateService.createTemplate(createRequest))
                .isInstanceOf(ServiceException.class)
                .satisfies(throwable -> {
                    ServiceException exception = (ServiceException) throwable;
                    assertThat(exception.getErrors()).isNotEmpty();
                    assertThat(exception.getErrors().get(0).getReasonCode())
                            .isEqualTo(TemplateServiceExceptionReasonCodes.TEMPLATE_EXISTS.getErrorCode());
                });
    }

    @Test
    void getTemplate_WhenExists_ShouldReturnTemplate() {
        when(templateRepository.findByTemplateIdAndLanguageAndIsDeletedFalse("welcome-email", "en"))
                .thenReturn(Optional.of(testTemplate));

        TemplateResponse response = templateService.getTemplate("welcome-email", "en");

        assertThat(response).isNotNull();
        assertThat(response.getTemplateId()).isEqualTo("welcome-email");
        assertThat(response.getLanguage()).isEqualTo("en");
        assertThat(response.getName()).isEqualTo("Welcome Email");
    }

    @Test
    void getTemplate_WhenNotFound_ShouldThrowException() {
        when(templateRepository.findByTemplateIdAndLanguageAndIsDeletedFalse("nonexistent", "en"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> templateService.getTemplate("nonexistent", "en"))
                .isInstanceOf(ServiceException.class)
                .satisfies(throwable -> {
                    ServiceException exception = (ServiceException) throwable;
                    assertThat(exception.getErrors()).isNotEmpty();
                    assertThat(exception.getErrors().get(0).getReasonCode())
                            .isEqualTo(TemplateServiceExceptionReasonCodes.TEMPLATE_NOT_FOUND.getErrorCode());
                });
    }

    @Test
    void updateTemplate_WhenValid_ShouldUpdateTemplate() {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            UserPrincipal mockPrincipal = mock(UserPrincipal.class);
            when(mockPrincipal.getUserId()).thenReturn(testUserId);
            securityUtils.when(SecurityUtils::getUserPrincipal).thenReturn(mockPrincipal);

            UpdateTemplateRequest updateRequest = new UpdateTemplateRequest()
                    .name("Updated Welcome Email")
                    .description("Updated description")
                    .content("<h1>Welcome, {{user.name}}!</h1>");

            when(templateRepository.findByTemplateIdAndLanguageAndIsDeletedFalse("welcome-email", "en"))
                    .thenReturn(Optional.of(testTemplate));
            when(templateRepository.save(any(TemplateEntity.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            TemplateResponse response = templateService.updateTemplate("welcome-email", "en", updateRequest);

            assertThat(response).isNotNull();
            assertThat(response.getName()).isEqualTo("Updated Welcome Email");
            verify(templateValidator).validateSyntax(updateRequest.getContent());
            verify(templateRepository).save(testTemplate);
            verify(renderingEngine).invalidateCache("welcome-email:en");
        }
    }

    @Test
    void deleteTemplate_WhenExists_ShouldSoftDelete() {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            UserPrincipal mockPrincipal = mock(UserPrincipal.class);
            when(mockPrincipal.getUserId()).thenReturn(testUserId);
            securityUtils.when(SecurityUtils::getUserPrincipal).thenReturn(mockPrincipal);

            when(templateRepository.findByTemplateIdAndLanguageAndIsDeletedFalse("welcome-email", "en"))
                    .thenReturn(Optional.of(testTemplate));
            when(templateRepository.save(any(TemplateEntity.class)))
                    .thenReturn(testTemplate);

            templateService.deleteTemplate("welcome-email", "en");

            assertThat(testTemplate.getIsDeleted()).isTrue();
            verify(templateRepository).save(testTemplate);
            verify(renderingEngine).invalidateCache("welcome-email:en");
        }
    }

    @Test
    void renderTemplate_WhenValid_ShouldRenderSuccessfully() {
        RenderTemplateRequest renderRequest = new RenderTemplateRequest()
                .params(Map.of("user", Map.of("firstName", "John")));

        when(templateRepository.findByTemplateIdAndLanguageAndIsDeletedFalse("welcome-email", "en"))
                .thenReturn(Optional.of(testTemplate));
        when(renderingEngine.render(eq("welcome-email:en"), anyString(), anyMap()))
                .thenReturn("<h1>Welcome, John!</h1>");

        RenderTemplateResponse response = templateService.renderTemplate("welcome-email", "en", renderRequest);

        assertThat(response).isNotNull();
        assertThat(response.getHtml()).contains("Welcome, John!");
        verify(renderingEngine).render(eq("welcome-email:en"), eq(testTemplate.getContent()), eq(renderRequest.getParams()));
    }

    @Test
    void renderTemplate_WhenMissingParams_ShouldThrowException() {
        RenderTemplateRequest renderRequest = new RenderTemplateRequest()
                .params(Map.of());

        when(templateRepository.findByTemplateIdAndLanguageAndIsDeletedFalse("welcome-email", "en"))
                .thenReturn(Optional.of(testTemplate));

        assertThatThrownBy(() -> templateService.renderTemplate("welcome-email", "en", renderRequest))
                .isInstanceOf(ServiceException.class)
                .satisfies(throwable -> {
                    ServiceException exception = (ServiceException) throwable;
                    assertThat(exception.getErrors()).isNotEmpty();
                    assertThat(exception.getErrors().get(0).getReasonCode())
                            .isEqualTo(TemplateServiceExceptionReasonCodes.MISSING_REQUIRED_PARAMS.getErrorCode());
                });
    }

    @Test
    void getTemplateMetadata_WhenExists_ShouldReturnMetadata() {
        when(templateRepository.findByTemplateIdAndLanguageAndIsDeletedFalse("welcome-email", "en"))
                .thenReturn(Optional.of(testTemplate));

        TemplateMetadataResponse response = templateService.getTemplateMetadata("welcome-email", "en");

        assertThat(response).isNotNull();
        assertThat(response.getTemplateId()).isEqualTo("welcome-email");
        assertThat(response.getParamSchema()).isNotEmpty();
        assertThat(response.getParamSchema()).containsKey("user");
    }
}
