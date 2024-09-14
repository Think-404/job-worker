package org.originit.service;

import org.originit.config.AIConfig;

public interface AIService {

    String sendRequest(AIConfig aiConfig, String message);
}
