package com.corems.templatems.app.service;

import com.corems.common.exception.ServiceException;
import com.corems.templatems.app.exception.TemplateServiceExceptionReasonCodes;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class HandlebarsRenderingEngine {

    private final Handlebars handlebars;
    private final Map<String, Template> templateCache;

    public HandlebarsRenderingEngine() {
        this.handlebars = new Handlebars();
        this.templateCache = new ConcurrentHashMap<>();
    }

    public String render(String templateId, String templateContent, Map<String, Object> params) {
        try {
            Template template = getOrCompileTemplate(templateId, templateContent);
            return template.apply(params);
        } catch (IOException e) {
            throw ServiceException.of(
                TemplateServiceExceptionReasonCodes.TEMPLATE_RENDERING_FAILED,
                "Failed to render template '" + templateId + "': " + e.getMessage()
            );
        }
    }

    public void invalidateCache(String templateId) {
        templateCache.remove(templateId);
    }

    public void clearCache() {
        templateCache.clear();
    }

    private Template getOrCompileTemplate(String templateId, String templateContent) throws IOException {
        return templateCache.computeIfAbsent(templateId, id -> {
            try {
                return handlebars.compileInline(templateContent);
            } catch (IOException e) {
                throw ServiceException.of(
                    TemplateServiceExceptionReasonCodes.TEMPLATE_COMPILATION_FAILED,
                    "Failed to compile template '" + templateId + "': " + e.getMessage()
                );
            }
        });
    }
}
