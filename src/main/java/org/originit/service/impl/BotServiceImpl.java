package org.originit.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.fluent.Request;
import org.originit.service.BotService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
@Component
public class BotServiceImpl implements BotService {

    @Value("${bot.hook-url}")
    private String botUrl;

    @Value("${bot.enable}")
    private boolean botEnable;

    public void sendMessage(String message) {
        if (!botEnable) {
            return;
        }
        // 发送HTTP请求
        try {
            String response = Request.post(botUrl)
                    .bodyString("{\"msg_type\": \"text\", \"content\": {\"text\": \"" + message + "\"}}",
                            org.apache.hc.core5.http.ContentType.APPLICATION_JSON)
                    .execute()
                    .returnContent()
                    .asString();
            log.info("消息推送成功: {}", response);
        } catch (Exception e) {
            log.error("消息推送失败: {}", e.getMessage());
        }
    }

    @Override
    public void sendMessageWithDateTime(String message) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateStr = sdf.format(new Date());
        sendMessage(dateStr + " " + message);
    }


    /**
     * 通用的安全类型转换方法，避免未检查的类型转换警告
     *
     * @param obj   要转换的对象
     * @param clazz 目标类型的 Class 对象
     * @param <T>   目标类型
     * @return 如果对象类型匹配，则返回转换后的对象，否则返回 null
     */
    @SuppressWarnings("unchecked")
    public static <T> T safeCast(Object obj, Class<T> clazz) {
        if (clazz.isInstance(obj)) {
            return (T) obj;
        } else {
            return null;
        }
    }
}
