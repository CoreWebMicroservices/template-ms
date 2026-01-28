package com.corems.templatems.app.controller;

import com.corems.templatems.api.model.RenderTemplateRequest;
import com.corems.templatems.api.model.RenderTemplateResponse;
import com.corems.templatems.app.service.TemplateRenderingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TemplateRenderingControllerTest {

    @Mock
    private TemplateRenderingService templateRenderingService;

    @InjectMocks
    private TemplateRenderingController templateRenderingController;

    @Test
    void renderTemplate_ShouldReturnRenderedHtml() {
        RenderTemplateRequest request = new RenderTemplateRequest();
        request.setParams(Map.of("name", "John"));

        RenderTemplateResponse response = new RenderTemplateResponse();
        response.setHtml("<html><body>Hello John!</body></html>");

        when(templateRenderingService.renderTemplate("test-template", null, request))
            .thenReturn(response);

        ResponseEntity<RenderTemplateResponse> result = templateRenderingController.renderTemplate(
            "test-template",
            request,
            Optional.empty()
        );

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getHtml()).contains("Hello John!");
    }
}
