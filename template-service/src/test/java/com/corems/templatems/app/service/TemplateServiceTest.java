package com.corems.templatems.app.service;

import com.corems.templatems.app.repository.TemplateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class TemplateManagementServiceTest {

    @Mock
    private TemplateRepository templateRepository;

    @Mock
    private TemplateValidator templateValidator;

    @InjectMocks
    private TemplateManagementService templateManagementService;

    @Test
    void serviceCanBeInstantiated() {
        assertThat(templateManagementService).isNotNull();
    }
}
