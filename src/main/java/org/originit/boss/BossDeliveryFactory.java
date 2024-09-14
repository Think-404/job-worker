package org.originit.boss;

import lombok.RequiredArgsConstructor;
import org.originit.config.AIConfig;
import org.originit.config.Configuration;
import org.originit.executor.DeliverExecutor;
import org.originit.executor.DeliveryFactory;
import org.originit.filter.Filters;
import org.originit.infra.driver.ChromeDriverManager;
import org.originit.manager.CookieManager;
import org.originit.service.BotService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@ConditionalOnProperty(name = "platform", havingValue = "boss")
@Component
@RequiredArgsConstructor
public class BossDeliveryFactory implements DeliveryFactory {

    private final Configuration configuration;

    private final ChromeDriverManager driverManager;

    private final CookieManager cookieManager;

    private final Filters filters;

    private final AIConfig aiConfig;

    private final BotService botService;

    @Override
    public DeliverExecutor getDeliverExecutor() {
        return new Boss(configuration, aiConfig, driverManager.getDriver(), cookieManager, filters, botService);
    }
}
