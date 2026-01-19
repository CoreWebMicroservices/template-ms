package com.corems.templatems.app.exception;

import com.corems.common.exception.handler.ExceptionReasonCodes;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
@ToString
public enum TemplateServiceExceptionReasonCodes implements ExceptionReasonCodes {

    TEMPLATE_EXISTS("template.exists", HttpStatus.CONFLICT, "Template already exists"),
    TEMPLATE_NOT_FOUND("template.not_found", HttpStatus.NOT_FOUND, "Template not found"),
    INVALID_TEMPLATE_SYNTAX("template.invalid_syntax", HttpStatus.BAD_REQUEST, "Invalid template syntax"),
    TEMPLATE_COMPILATION_FAILED("template.compilation_failed", HttpStatus.BAD_REQUEST, "Template compilation failed"),
    TEMPLATE_RENDERING_FAILED("template.rendering_failed", HttpStatus.INTERNAL_SERVER_ERROR, "Template rendering failed"),
    MISSING_REQUIRED_PARAMS("template.missing_params", HttpStatus.BAD_REQUEST, "Missing required parameters");

    private final String errorCode;
    private final HttpStatus httpStatus;
    private final String description;
}
