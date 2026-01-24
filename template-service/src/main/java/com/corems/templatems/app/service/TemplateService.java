package com.corems.templatems.app.service;

import com.corems.common.exception.ServiceException;
import com.corems.common.security.SecurityUtils;
import com.corems.common.security.UserPrincipal;
import com.corems.common.utils.db.utils.QueryParams;
import com.corems.templatems.app.exception.TemplateServiceExceptionReasonCodes;
import com.corems.templatems.api.model.CreateTemplateRequest;
import com.corems.templatems.api.model.RenderTemplateRequest;
import com.corems.templatems.api.model.RenderTemplateResponse;
import com.corems.templatems.api.model.TemplateMetadataResponse;
import com.corems.templatems.api.model.TemplatePagedResponse;
import com.corems.templatems.api.model.TemplateParamDefinition;
import com.corems.templatems.api.model.TemplateResponse;
import com.corems.templatems.api.model.UpdateTemplateRequest;
import com.corems.templatems.app.entity.TemplateEntity;
import com.corems.templatems.app.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateService {

    private final TemplateRepository templateRepository;
    private final TemplateValidator templateValidator;
    private final RenderingEngine renderingEngine;

    @Value("${template-service.default-language:en}")
    private String defaultLanguage;

    @Transactional
    public TemplateResponse createTemplate(CreateTemplateRequest request) {
        String language = request.getLanguage() != null ? request.getLanguage() : defaultLanguage;

        if (templateRepository.findByTemplateIdAndLanguageAndIsDeletedFalse(request.getTemplateId(), language).isPresent()) {
            throw ServiceException.of(TemplateServiceExceptionReasonCodes.TEMPLATE_EXISTS, 
                "Template with ID '" + request.getTemplateId() + "' and language '" + language + "' already exists");
        }

        templateValidator.validateSyntax(request.getContent());

        Map<String, Object> paramSchema = convertParamSchema(request.getParamSchema());
        if (paramSchema == null || paramSchema.isEmpty()) {
            paramSchema = templateValidator.extractParameters(request.getContent());
        }

        UserPrincipal currentUser = SecurityUtils.getUserPrincipal();
        UUID currentUserId = currentUser.getUserId();

        TemplateEntity entity = TemplateEntity.builder()
                .templateId(request.getTemplateId())
                .name(request.getName())
                .description(request.getDescription())
                .content(request.getContent())
                .category(request.getCategory())
                .language(language)
                .paramSchema(paramSchema)
                .createdBy(currentUserId)
                .updatedBy(currentUserId)
                .build();

        entity = templateRepository.save(entity);

        log.info("Created template: {} (language: {}) by user: {}", entity.getTemplateId(), language, currentUserId);

        return mapToResponse(entity);
    }

    @Transactional(readOnly = true)
    public TemplateResponse getTemplateById(UUID id) {
        TemplateEntity entity = templateRepository.findByUuidAndIsDeletedFalse(id)
                .orElseThrow(() -> ServiceException.of(TemplateServiceExceptionReasonCodes.TEMPLATE_NOT_FOUND, 
                    "Template with ID '" + id + "' not found"));

        return mapToResponse(entity);
    }

    @Transactional(readOnly = true)
    public TemplateResponse getTemplateByTemplateId(String templateId, String language) {
        String effectiveLanguage = language != null ? language : defaultLanguage;

        TemplateEntity entity = templateRepository.findByTemplateIdAndLanguageAndIsDeletedFalse(templateId, effectiveLanguage)
                .orElseThrow(() -> ServiceException.of(TemplateServiceExceptionReasonCodes.TEMPLATE_NOT_FOUND, 
                    "Template '" + templateId + "' with language '" + effectiveLanguage + "' not found"));

        return mapToResponse(entity);
    }

    @Transactional(readOnly = true)
    public TemplatePagedResponse listTemplates(Optional<Integer> page, Optional<Integer> pageSize, Optional<String> sort, Optional<String> search, Optional<List<String>> filter) {
        QueryParams params = new QueryParams(page, pageSize, search, sort, filter);
        
        Page<TemplateEntity> templatePage = templateRepository.findAllByQueryParams(params);
        List<TemplateResponse> items = templatePage.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        TemplatePagedResponse response = new TemplatePagedResponse(templatePage.getNumber() + 1, templatePage.getSize());
        response.setItems(items);
        response.setTotalPages(templatePage.getTotalPages());
        response.setTotalElements(templatePage.getTotalElements());
        return response;
    }

    @Transactional
    public TemplateResponse updateTemplateById(UUID id, UpdateTemplateRequest request) {
        TemplateEntity entity = templateRepository.findByUuidAndIsDeletedFalse(id)
                .orElseThrow(() -> ServiceException.of(TemplateServiceExceptionReasonCodes.TEMPLATE_NOT_FOUND, 
                    "Template with ID '" + id + "' not found"));

        String oldTemplateId = entity.getTemplateId();
        String oldLanguage = entity.getLanguage();
        boolean contentChanged = false;
        boolean identifierChanged = false;

        if (request.getTemplateId() != null && !request.getTemplateId().equals(entity.getTemplateId())) {
            Optional<TemplateEntity> existing = templateRepository.findByTemplateIdAndLanguageAndIsDeletedFalse(
                request.getTemplateId(), 
                request.getLanguage() != null ? request.getLanguage() : entity.getLanguage()
            );
            if (existing.isPresent() && !existing.get().getUuid().equals(id)) {
                throw ServiceException.of(TemplateServiceExceptionReasonCodes.TEMPLATE_EXISTS, 
                    "Template with ID '" + request.getTemplateId() + "' and language '" + 
                    (request.getLanguage() != null ? request.getLanguage() : entity.getLanguage()) + "' already exists");
            }
            entity.setTemplateId(request.getTemplateId());
            identifierChanged = true;
        }

        if (request.getLanguage() != null && !request.getLanguage().equals(entity.getLanguage())) {
            Optional<TemplateEntity> existing = templateRepository.findByTemplateIdAndLanguageAndIsDeletedFalse(
                request.getTemplateId() != null ? request.getTemplateId() : entity.getTemplateId(),
                request.getLanguage()
            );
            if (existing.isPresent() && !existing.get().getUuid().equals(id)) {
                throw ServiceException.of(TemplateServiceExceptionReasonCodes.TEMPLATE_EXISTS, 
                    "Template with ID '" + (request.getTemplateId() != null ? request.getTemplateId() : entity.getTemplateId()) + 
                    "' and language '" + request.getLanguage() + "' already exists");
            }
            entity.setLanguage(request.getLanguage());
            identifierChanged = true;
        }

        if (request.getName() != null) {
            entity.setName(request.getName());
        }

        if (request.getDescription() != null) {
            entity.setDescription(request.getDescription());
        }

        if (request.getContent() != null) {
            templateValidator.validateSyntax(request.getContent());
            entity.setContent(request.getContent());
            contentChanged = true;
        }

        if (request.getCategory() != null) {
            entity.setCategory(request.getCategory());
        }

        if (request.getParamSchema() != null) {
            entity.setParamSchema(convertParamSchema(request.getParamSchema()));
        } else if (contentChanged) {
            Map<String, Object> extractedParams = templateValidator.extractParameters(entity.getContent());
            entity.setParamSchema(extractedParams);
        }

        UserPrincipal currentUser = SecurityUtils.getUserPrincipal();
        entity.setUpdatedBy(currentUser.getUserId());

        entity = templateRepository.save(entity);

        if (contentChanged || identifierChanged) {
            renderingEngine.invalidateCache(oldTemplateId + ":" + oldLanguage);
            if (identifierChanged) {
                renderingEngine.invalidateCache(entity.getTemplateId() + ":" + entity.getLanguage());
            }
        }

        log.info("Updated template: {} (language: {}) by user: {}", entity.getTemplateId(), entity.getLanguage(), currentUser.getUserId());

        return mapToResponse(entity);
    }

    @Transactional
    public void deleteTemplateById(UUID id) {
        TemplateEntity entity = templateRepository.findByUuidAndIsDeletedFalse(id)
                .orElseThrow(() -> ServiceException.of(TemplateServiceExceptionReasonCodes.TEMPLATE_NOT_FOUND, 
                    "Template with ID '" + id + "' not found"));

        entity.setIsDeleted(true);
        UserPrincipal currentUser = SecurityUtils.getUserPrincipal();
        entity.setUpdatedBy(currentUser.getUserId());
        templateRepository.save(entity);

        renderingEngine.invalidateCache(entity.getTemplateId() + ":" + entity.getLanguage());

        log.info("Deleted template: {} (language: {})", entity.getTemplateId(), entity.getLanguage());
    }

    @Transactional(readOnly = true)
    public RenderTemplateResponse renderTemplate(String templateId, String language, RenderTemplateRequest request) {
        String effectiveLanguage = language != null ? language : defaultLanguage;

        TemplateEntity entity = templateRepository.findByTemplateIdAndLanguageAndIsDeletedFalse(templateId, effectiveLanguage)
                .orElseThrow(() -> ServiceException.of(TemplateServiceExceptionReasonCodes.TEMPLATE_NOT_FOUND, 
                    "Template '" + templateId + "' with language '" + effectiveLanguage + "' not found"));

        validateRenderParams(entity, request.getParams());

        String html = renderingEngine.render(templateId + ":" + effectiveLanguage, entity.getContent(), request.getParams());

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

    private Map<String, Object> convertParamSchema(Map<String, TemplateParamDefinition> paramDefMap) {
        if (paramDefMap == null) {
            return null;
        }

        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, TemplateParamDefinition> entry : paramDefMap.entrySet()) {
            TemplateParamDefinition def = entry.getValue();
            Map<String, Object> defMap = new HashMap<>();
            if (def.getRequired() != null) {
                defMap.put("required", def.getRequired());
            }
            if (def.getType() != null) {
                defMap.put("type", def.getType().getValue());
            }
            if (def.getPattern() != null) {
                defMap.put("pattern", def.getPattern());
            }
            result.put(entry.getKey(), defMap);
        }
        return result;
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
