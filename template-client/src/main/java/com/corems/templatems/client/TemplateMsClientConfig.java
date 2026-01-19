package com.corems.templatems.client;

import com.corems.templatems.ApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

@AutoConfiguration
public class TemplateMsClientConfig {

    @Value("${templatems.base-url:http://localhost:3004}")
    private String templateMsBaseUrl;

    @Bean(name = "templateRestClient")
    @ConditionalOnMissingBean(name = "templateRestClient")
    public RestClient templateRestClient(RestClient.Builder inboundRestClientBuilder) {
        return inboundRestClientBuilder
                .baseUrl(templateMsBaseUrl)
                .build();
    }

    @Bean(name = "templateApiClient")
    @ConditionalOnMissingBean(name = "templateApiClient")
    public ApiClient templateApiClient(RestClient templateRestClient) {
        return new ApiClient(templateRestClient);
    }

    @Bean
    @ConditionalOnMissingBean(name = "templatesApi")
    public TemplatesApi templatesApi(ApiClient templateApiClient) {
        return new TemplatesApi(templateApiClient);
    }

    @Bean
    @ConditionalOnMissingBean(name = "renderingApi")
    public RenderingApi renderingApi(ApiClient templateApiClient) {
        return new RenderingApi(templateApiClient);
    }
}
