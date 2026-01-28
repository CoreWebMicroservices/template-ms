package com.corems.templatems.app.controller;

import com.corems.common.security.CoreMsRoles;
import com.corems.common.security.RequireRoles;
import com.corems.templatems.api.TemplateManagementApi;
import com.corems.templatems.api.model.CreateTemplateRequest;
import com.corems.templatems.api.model.SuccessfulResponse;
import com.corems.templatems.api.model.TemplatePagedResponse;
import com.corems.templatems.api.model.TemplateResponse;
import com.corems.templatems.api.model.UpdateTemplateRequest;
import com.corems.templatems.app.service.TemplateManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequireRoles(CoreMsRoles.TEMPLATE_MS_ADMIN)
public class TemplateManagementController implements TemplateManagementApi {

    private final TemplateManagementService templateManagementService;

    @Override
    public ResponseEntity<TemplateResponse> createTemplate(CreateTemplateRequest createTemplateRequest) {
        TemplateResponse response = templateManagementService.createTemplate(createTemplateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<SuccessfulResponse> deleteTemplate(UUID id) {
        templateManagementService.deleteTemplateById(id);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<TemplateResponse> getTemplate(UUID id) {
        TemplateResponse response = templateManagementService.getTemplateById(id);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<TemplatePagedResponse> listTemplates(Optional<Integer> page, Optional<Integer> pageSize, Optional<String> sort, Optional<String> search, Optional<List<String>> filter) {
        TemplatePagedResponse response = templateManagementService.listTemplates(page, pageSize, sort, search, filter);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<TemplateResponse> updateTemplate(UUID id, UpdateTemplateRequest updateTemplateRequest) {
        TemplateResponse response = templateManagementService.updateTemplateById(id, updateTemplateRequest);
        return ResponseEntity.ok(response);
    }
}
