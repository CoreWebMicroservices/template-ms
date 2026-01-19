package com.corems.templatems.app.controller;

import com.corems.common.security.CoreMsRoles;
import com.corems.common.security.RequireRoles;
import com.corems.templatems.api.RenderingApi;
import com.corems.templatems.api.TemplatesApi;
import com.corems.templatems.api.model.CreateTemplateRequest;
import com.corems.templatems.api.model.RenderTemplateRequest;
import com.corems.templatems.api.model.RenderTemplateResponse;
import com.corems.templatems.api.model.SuccessfulResponse;
import com.corems.templatems.api.model.TemplateMetadataResponse;
import com.corems.templatems.api.model.TemplatePagedResponse;
import com.corems.templatems.api.model.TemplateResponse;
import com.corems.templatems.api.model.UpdateTemplateRequest;
import com.corems.templatems.app.service.TemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TemplateController implements TemplatesApi, RenderingApi {

    private final TemplateService templateService;

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return TemplatesApi.super.getRequest();
    }

    @Override
    @RequireRoles(CoreMsRoles.TEMPLATE_MS_ADMIN)
    public ResponseEntity<TemplateResponse> createTemplate(CreateTemplateRequest createTemplateRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(templateService.createTemplate(createTemplateRequest));
    }

    @Override
    public ResponseEntity<TemplateResponse> getTemplate(String templateId, Optional<String> language) {
        return ResponseEntity.ok(templateService.getTemplate(templateId, language.orElse(null)));
    }

    @Override
    public ResponseEntity<TemplatePagedResponse> listTemplates(Optional<Integer> page, Optional<Integer> pageSize, Optional<String> sort, Optional<String> search, Optional<List<String>> filter) {
        return ResponseEntity.ok(templateService.listTemplates(page, pageSize, sort, search, filter));
    }

    @Override
    @RequireRoles(CoreMsRoles.TEMPLATE_MS_ADMIN)
    public ResponseEntity<TemplateResponse> updateTemplate(String templateId, UpdateTemplateRequest updateTemplateRequest, Optional<String> language) {
        return ResponseEntity.ok(templateService.updateTemplate(templateId, language.orElse(null), updateTemplateRequest));
    }

    @Override
    @RequireRoles(CoreMsRoles.TEMPLATE_MS_ADMIN)
    public ResponseEntity<SuccessfulResponse> deleteTemplate(String templateId, Optional<String> language) {
        templateService.deleteTemplate(templateId, language.orElse(null));
        return ResponseEntity.ok(new SuccessfulResponse().result(true));
    }

    @Override
    public ResponseEntity<RenderTemplateResponse> renderTemplate(String templateId, RenderTemplateRequest renderTemplateRequest, Optional<String> language) {
        return ResponseEntity.ok(templateService.renderTemplate(templateId, language.orElse(null), renderTemplateRequest));
    }

    @Override
    public ResponseEntity<TemplateMetadataResponse> getTemplateMetadata(String templateId, Optional<String> language) {
        return ResponseEntity.ok(templateService.getTemplateMetadata(templateId, language.orElse(null)));
    }
}
