package com.corems.templatems.app.controller;

import com.corems.templatems.api.model.CreateTemplateRequest;
import com.corems.templatems.api.model.TemplateResponse;
import com.corems.templatems.app.service.TemplateManagementService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TemplateManagementControllerTest {

    @Mock
    private TemplateManagementService templateManagementService;

    @InjectMocks
    private TemplateManagementController templateManagementController;

    @Test
    void createTemplate_ShouldReturnCreatedStatus() {
        CreateTemplateRequest request = new CreateTemplateRequest();
        request.setTemplateId("test-template");
        request.setName("Test Template");
        request.setContent("<html>{{name}}</html>");

        TemplateResponse response = new TemplateResponse();
        response.setId(UUID.randomUUID());
        response.setTemplateId("test-template");
        response.setName("Test Template");

        when(templateManagementService.createTemplate(any(CreateTemplateRequest.class)))
            .thenReturn(response);

        ResponseEntity<TemplateResponse> result = templateManagementController.createTemplate(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getTemplateId()).isEqualTo("test-template");
    }
}
