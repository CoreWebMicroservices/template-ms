package com.corems.templatems.app.controller;

import com.corems.templatems.api.TemplateRenderingApi;
import com.corems.templatems.api.model.RenderTemplateRequest;
import com.corems.templatems.api.model.RenderTemplateResponse;
import com.corems.templatems.api.model.TemplateMetadataResponse;
import com.corems.templatems.api.model.TemplateResponse;
import com.corems.templatems.app.service.TemplateRenderingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class TemplateRenderingController implements TemplateRenderingApi {

    private final TemplateRenderingService templateRenderingService;

    @Override
    public ResponseEntity<TemplateResponse> getTemplateByTemplateId(String templateId, Optional<String> language) {
        TemplateResponse response = templateRenderingService.getTemplateByTemplateId(templateId, language.orElse(null));
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<TemplateMetadataResponse> getTemplateMetadata(String templateId, Optional<String> language) {
        TemplateMetadataResponse response = templateRenderingService.getTemplateMetadata(templateId, language.orElse(null));
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<RenderTemplateResponse> renderTemplate(String templateId, RenderTemplateRequest renderTemplateRequest, Optional<String> language) {
        RenderTemplateResponse response = templateRenderingService.renderTemplate(templateId, language.orElse(null), renderTemplateRequest);
        return ResponseEntity.ok(response);
    }
}
