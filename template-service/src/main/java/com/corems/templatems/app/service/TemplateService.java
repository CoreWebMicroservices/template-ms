package com.corems.templatems.app.service;

import com.corems.common.exception.ServiceException;
import com.corems.common.security.SecurityUtils;
import com.corems.common.security.UserPrincipal;
import com.corems.common.utils.db.utils.PaginatedQueryExecutor;
import com.corems.templatems.app.exception.TemplateServiceExceptionReasonCodes;
import com.corems.templatems.api.model.CreateTemplateRequest;
import com.corems.templatems.api.model.RenderTemplateRequest;
import com.corems.templatems.api.model.RenderTemplateResponse;
import com.corems.templatems.api.model.TemplateMetadataResponse;
import com.corems.templatems.api.model.TemplatePagedResponse;
import com.corems.templatems.api.model.TemplateResponse;
import com.corems.templatems.api.model.UpdateTemplateRequest;
import com.corems.templatems.app.entity.TemplateEntity;
import com.corems.templatems.app.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateService {

    private final TemplateRepository templateRepository;
    private final TemplateValidator templateValidator;
    private final HandlebarsRenderingEngine renderingEngine;

    @Transactional
    public TemplateResponse createTemplate(CreateTemplateRequest request) {
        if (templateRepository.findByTemplateIdAndIsDeletedFalse(request.getTemplateId()).isPresent()) {
            throw ServiceException.of(TemplateServiceExceptionReasonCodes.TEMPLATE_EXISTS, 
                "Template with ID '" + request.getTemplateId() + "' already exists");
        }

        templateValidator.validateSyntax(request.getContent());

        Map<String, Object> paramSchema = request.getParamSchema();
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
                .paramSchema(paramSchema)
                .createdBy(currentUserId)
                .updatedBy(currentUserId)
                .build();

        entity = templateRepository.save(entity);

        log.info("Created template: {} by user: {}", entity.getTemplateId(), currentUserId);

        return mapToResponse(entity);
    }

    @Transactional(readOnly = true)
    public TemplateResponse getTemplate(String templateId) {
        TemplateEntity entity = templateRepository.findByTemplateIdAndIsDeletedFalse(templateId)
                .orElseThrow(() -> ServiceException.of(TemplateServiceExceptionReasonCodes.TEMPLATE_NOT_FOUND, 
                    "Template '" + templateId + "' not found"));

        return mapToResponse(entity);
    }

    @Transactional(readOnly = true)
    public TemplatePagedResponse listTemplates(Integer page, Integer pageSize, String sort, String search, String filter) {
        return PaginatedQueryExecutor.execute(
                templateRepository,
                page, pageSize, sort, search, filter,
                this::mapToResponse
        );
    }

    @Transactional
    public TemplateResponse updateTemplate(String templateId, UpdateTemplateRequest request) {
        TemplateEntity entity = templateRepository.findByTemplateIdAndIsDeletedFalse(templateId)
                .orElseThrow(() -> ServiceException.of(TemplateServiceExceptionReasonCodes.TEMPLATE_NOT_FOUND, 
                    "Template '" + templateId + "' not found"));

        boolean contentChanged = false;

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
            entity.setParamSchema(request.getParamSchema());
        } else if (contentChanged) {
            Map<String, Object> extractedParams = templateValidator.extractParameters(entity.getContent());
            entity.setParamSchema(extractedParams);
        }

        UserPrincipal currentUser = SecurityUtils.getUserPrincipal();
        entity.setUpdatedBy(currentUser.getUserId());

        entity = templateRepository.save(entity);

        if (contentChanged) {
            renderingEngine.invalidateCache(templateId);
        }

        log.info("Updated template: {} by user: {}", templateId, currentUser.getUserId());

        return mapToResponse(entity);
    }

    @Transactional
    public void deleteTemplate(String templateId) {
        TemplateEntity entity = templateRepository.findByTemplateIdAndIsDeletedFalse(templateId)
                .orElseThrow(() -> ServiceException.of(TemplateServiceExceptionReasonCodes.TEMPLATE_NOT_FOUND, 
                    "Template '" + templateId + "' not found"));

        entity.setIsDeleted(true);
        UserPrincipal currentUser = SecurityUtils.getUserPrincipal();
        entity.setUpdatedBy(currentUser.getUserId());
        templateRepository.save(entity);

        renderingEngine.invalidateCache(templateId);

        log.info("Deleted template: {}", templateId);
    }

    @Transactional(readOnly = true)
    public RenderTemplateResponse renderTemplate(String templateId, RenderTemplateRequest request) {
        TemplateEntity entity = templateRepository.findByTemplateIdAndIsDeletedFalse(templateId)
                .orElseThrow(() -> ServiceException.of(TemplateServiceExceptionReasonCodes.TEMPLATE_NOT_FOUND, 
                    "Template '" + templateId + "' not found"));

        validateRenderParams(entity, request.getParams());

        String html = renderingEngine.render(templateId, entity.getContent(), request.getParams());

        return new RenderTemplateResponse().html(html);
    }

    @Transactional(readOnly = true)
    public TemplateMetadataResponse getTemplateMetadata(String templateId) {
        TemplateEntity entity = templateRepository.findByTemplateIdAndIsDeletedFalse(templateId)
                .orElseThrow(() -> ServiceException.of(TemplateServiceExceptionReasonCodes.TEMPLATE_NOT_FOUND, 
                    "Template '" + templateId + "' not found"));

        return new TemplateMetadataResponse()
                .templateId(entity.getTemplateId())
                .name(entity.getName())
                .description(entity.getDescription())
                .category(entity.getCategory())
                .paramSchema(entity.getParamSchema());
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

    private TemplateResponse mapToResponse(TemplateEntity entity) {
        return new TemplateResponse()
                .id(entity.getUuid())
                .templateId(entity.getTemplateId())
                .name(entity.getName())
                .description(entity.getDescription())
                .content(entity.getContent())
                .category(entity.getCategory())
                .paramSchema(entity.getParamSchema())
                .createdAt(entity.getCreatedAt().atOffset(ZoneOffset.UTC))
                .updatedAt(entity.getUpdatedAt().atOffset(ZoneOffset.UTC))
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy());
    }
}
