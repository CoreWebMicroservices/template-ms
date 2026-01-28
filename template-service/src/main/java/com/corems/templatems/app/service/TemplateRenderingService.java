package com.corems.templatems.app.service;

import com.corems.common.exception.ServiceException;
import com.corems.templatems.app.exception.TemplateServiceExceptionReasonCodes;
import com.corems.templatems.api.model.RenderTemplateRequest;
import com.corems.templatems.api.model.RenderTemplateResponse;
import com.corems.templatems.api.model.TemplateMetadataResponse;
import com.corems.templatems.api.model.TemplateParamDefinition;
import com.corems.templatems.api.model.TemplateResponse;
import com.corems.templatems.app.entity.TemplateEntity;
import com.corems.templatems.app.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateRenderingService {

    private final TemplateRepository templateRepository;
    private final RenderingEngine renderingEngine;

    @Value("${template-service.default-language:en}")
    private String defaultLanguage;

    @Transactional(readOnly = true)
    public TemplateResponse getTemplateByTemplateId(String templateId, String language) {
        String effectiveLanguage = language != null ? language : defaultLanguage;

        TemplateEntity entity = templateRepository.findByTemplateIdAndLanguageAndIsDeletedFalse(templateId, effectiveLanguage)
                .orElseThrow(() -> ServiceException.of(TemplateServiceExceptionReasonCodes.TEMPLATE_NOT_FOUND, 
                    "Template '" + templateId + "' with language '" + effectiveLanguage + "' not found"));

        return mapToResponse(entity);
    }

    @Transactional(readOnly = true)
    public RenderTemplateResponse renderTemplate(String templateId, String language, RenderTemplateRequest request) {
        String effectiveLanguage = language != null ? language : defaultLanguage;

        TemplateEntity entity = templateRepository.findByTemplateIdAndLanguageAndIsDeletedFalse(templateId, effectiveLanguage)
                .orElseThrow(() -> ServiceException.of(TemplateServiceExceptionReasonCodes.TEMPLATE_NOT_FOUND, 
                    "Template '" + templateId + "' with language '" + effectiveLanguage + "' not found"));

        Map<String, Object> params = resolveTemplateVariables(request.getParams());
        validateRenderParams(entity, params);

        String html = renderingEngine.render(templateId + ":" + effectiveLanguage, entity.getContent(), params);

        return new RenderTemplateResponse().html(html);
    }

    @Transactional(readOnly = true)
    public TemplateMetadataResponse getTemplateMetadata(String templateId, String language) {
        String effectiveLanguage = language != null ? language : defaultLanguage;

        TemplateEntity entity = templateRepository.findByTemplateIdAndLanguageAndIsDeletedFalse(templateId, effectiveLanguage)
                .orElseThrow(() -> ServiceException.of(TemplateServiceExceptionReasonCodes.TEMPLATE_NOT_FOUND, 
                    "Template '" + templateId + "' with language '" + effectiveLanguage + "' not found"));

        return new TemplateMetadataResponse()
                .templateId(entity.getTemplateId())
                .name(entity.getName())
                .description(entity.getDescription())
                .category(entity.getCategory())
                .language(entity.getLanguage())
                .paramSchema(convertToParamDefinitionMap(entity.getParamSchema()));
    }

    private void validateRenderParams(TemplateEntity entity, Map<String, Object> params) {
        if (entity.getParamSchema() == null || entity.getParamSchema().isEmpty()) {
            return;
        }

        List<String> missingParams = new ArrayList<>();

        for (Map.Entry<String, Object> entry : entity.getParamSchema().entrySet()) {
            String paramName = entry.getKey();
            Object paramDef = entry.getValue();

            if (paramDef instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> defMap = (Map<String, Object>) paramDef;
                Boolean required = (Boolean) defMap.get("required");

                if (Boolean.TRUE.equals(required) && !params.containsKey(paramName)) {
                    missingParams.add(paramName);
                }
            }
        }

        if (!missingParams.isEmpty()) {
            throw ServiceException.of(TemplateServiceExceptionReasonCodes.MISSING_REQUIRED_PARAMS, 
                "Missing required parameters: " + String.join(", ", missingParams));
        }
    }

    private Map<String, Object> resolveTemplateVariables(Map<String, Object> params) {
        if (params == null) {
            return new HashMap<>();
        }

        Map<String, Object> resolved = new HashMap<>(params);
        List<String> keysToResolve = new ArrayList<>();

        for (String key : resolved.keySet()) {
            if (key.startsWith("template_")) {
                keysToResolve.add(key);
            }
        }

        for (String key : keysToResolve) {
            String templateId = key.substring("template_".length());
            TemplateEntity templateEntity = templateRepository.findByTemplateIdAndLanguageAndIsDeletedFalse(templateId, defaultLanguage)
                    .orElseThrow(() -> ServiceException.of(TemplateServiceExceptionReasonCodes.TEMPLATE_NOT_FOUND, 
                        "Referenced template '" + templateId + "' not found"));

            resolved.put(key, templateEntity.getContent());
        }

        return resolved;
    }

    private Map<String, TemplateParamDefinition> convertToParamDefinitionMap(Map<String, Object> paramSchema) {
        if (paramSchema == null) {
            return null;
        }

        Map<String, TemplateParamDefinition> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : paramSchema.entrySet()) {
            if (entry.getValue() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> defMap = (Map<String, Object>) entry.getValue();
                TemplateParamDefinition def = new TemplateParamDefinition();
                if (defMap.containsKey("required")) {
                    def.setRequired((Boolean) defMap.get("required"));
                }
                if (defMap.containsKey("type")) {
                    def.setType(TemplateParamDefinition.TypeEnum.fromValue((String) defMap.get("type")));
                }
                if (defMap.containsKey("pattern")) {
                    def.setPattern((String) defMap.get("pattern"));
                }
                result.put(entry.getKey(), def);
            }
        }
        return result;
    }

    private TemplateResponse mapToResponse(TemplateEntity entity) {
        return new TemplateResponse()
                .id(entity.getUuid())
                .templateId(entity.getTemplateId())
                .name(entity.getName())
                .description(entity.getDescription())
                .content(entity.getContent())
                .category(entity.getCategory())
                .language(entity.getLanguage())
                .paramSchema(convertToParamDefinitionMap(entity.getParamSchema()))
                .createdAt(entity.getCreatedAt().atOffset(ZoneOffset.UTC))
                .updatedAt(entity.getUpdatedAt().atOffset(ZoneOffset.UTC))
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy());
    }
}
