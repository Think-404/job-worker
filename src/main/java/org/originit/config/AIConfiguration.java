package org.originit.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "ai")
@Component
public class AIConfiguration implements AIConfig {

    private Boolean aiEnabled;

    private String aiModel = "gpt-3.5-turbo";

    private String apiKey;

    private String baseUrl;

    private String description;

}
