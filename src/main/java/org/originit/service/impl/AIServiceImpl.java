package org.originit.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.originit.config.AIConfig;
import org.originit.service.AIService;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Slf4j
@Component
public class AIServiceImpl implements AIService {

    public String sendRequest(AIConfig aiConfig, String content) {
        if (!Objects.equals(aiConfig.getAiEnabled(), true)) {
            return "Failed, AI is not enabled";
        }
        // 创建 HttpClient 实例
        HttpClient client = HttpClient.newHttpClient();

        // 构建 JSON 请求体
        JSONObject requestData = new JSONObject();
        requestData.put("model", aiConfig.getAiModel());
        requestData.put("temperature", 0.5);

        // 添加消息内容
        JSONArray messages = new JSONArray();
        JSONObject message = new JSONObject();
        message.put("role", "user");
        message.put("content", content);
        messages.put(message);

        requestData.put("messages", messages);

        // 构建 HTTP 请求
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(aiConfig.getBaseUrl() + "/v1/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + aiConfig.getApiKey())
                .POST(HttpRequest.BodyPublishers.ofString(requestData.toString()))
                .build();

        // 发送 HTTP 请求，并获取响应
        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                // 解析响应体

                log.info(response.body());
                JSONObject responseObject = new JSONObject(response.body());
                String requestId = responseObject.getString("id");
                long created = responseObject.getLong("created");
                String model = responseObject.getString("model");

                // 解析返回的内容
                JSONObject messageObject = responseObject.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message");
                String responseContent = messageObject.getString("content");

                // 解析 usage 部分
                JSONObject usageObject = responseObject.getJSONObject("usage");
                int promptTokens = usageObject.getInt("prompt_tokens");
                int completionTokens = usageObject.getInt("completion_tokens");
                int totalTokens = usageObject.getInt("total_tokens");

                // 格式化时间
                LocalDateTime createdTime = Instant.ofEpochSecond(created)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formattedTime = createdTime.format(formatter);

                log.info("请求ID: {}, 创建时间: {}, 模型名: {}, 提示词: {}, 补全: {}, 总用量: {}", requestId, formattedTime, model, promptTokens, completionTokens, totalTokens);
                return responseContent;
            } else {
                log.error("AI请求失败！");
            }
        } catch (Exception e) {
            log.error("AI请求异常！");
        }
        return "";
    }

}
