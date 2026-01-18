package com.corems.templatems.app.service;

import com.corems.common.exception.ServiceException;
import com.corems.templatems.app.exception.TemplateServiceExceptionReasonCodes;
import com.github.jknack.handlebars.Handlebars;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TemplateValidator {

    private final Handlebars handlebars;
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{([^{}#/]+)\\}\\}");

    public TemplateValidator() {
        this.handlebars = new Handlebars();
    }

    public void validateSyntax(String templateContent) {
        try {
            handlebars.compileInline(templateContent);
        } catch (IOException e) {
            throw ServiceException.of(
                TemplateServiceExceptionReasonCodes.INVALID_TEMPLATE_SYNTAX,
                "Invalid template syntax: " + e.getMessage()
            );
        }
    }

    public Map<String, Object> extractParameters(String templateContent) {
        Set<String> paramNames = new HashSet<>();
        Matcher matcher = VARIABLE_PATTERN.matcher(templateContent);
        
        while (matcher.find()) {
            String variable = matcher.group(1).trim();
            if (!isHelperOrBuiltin(variable)) {
                String paramName = extractParamName(variable);
                paramNames.add(paramName);
            }
        }
        
        Map<String, Object> paramSchema = new HashMap<>();
        for (String paramName : paramNames) {
            Map<String, Object> paramDef = new HashMap<>();
            paramDef.put("required", false);
            paramDef.put("type", "string");
            paramSchema.put(paramName, paramDef);
        }
        
        return paramSchema;
    }

    private boolean isHelperOrBuiltin(String variable) {
        return variable.startsWith("#") || 
               variable.startsWith("/") || 
               variable.startsWith("@") ||
               variable.equals("this") ||
               variable.equals(".");
    }

    private String extractParamName(String variable) {
        int dotIndex = variable.indexOf('.');
        if (dotIndex > 0) {
            return variable.substring(0, dotIndex);
        }
        return variable;
    }
}
