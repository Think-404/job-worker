package org.originit.service;

import org.originit.config.AIConfig;
import org.originit.model.Result;

public interface AIService {

    Result<String, String> sendRequest(AIConfig aiConfig, String message);
}
