package org.originit;

import lombok.extern.slf4j.Slf4j;
import org.originit.config.CustomConfiguration;
import org.originit.executor.DeliverExecutor;
import org.originit.executor.DeliveryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.CountDownLatch;

@SpringBootApplication
@Slf4j
public class Launcher {

    @Autowired
    private DeliveryFactory deliveryFactory;

    public static void main(String[] args) {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(Launcher.class);
        builder.web(WebApplicationType.NONE);
        builder.allowCircularReferences(true);
        SpringApplication app = builder.build();
        ConfigurableApplicationContext context = app.run(args);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            log.error("停止运行", e);
        }
    }

    @Scheduled(cron = "0 15 * * *")
    public void executeNewAccount() {
        log.info("executeNewAccount");
        try (DeliverExecutor deliverExecutor = deliveryFactory.createDeliverExecutor()) {
            deliverExecutor.execute(CustomConfiguration.builder()
                    .userId("newAccount")
                    .build());
        }

    }

    @Scheduled(cron = "0 10 * * *")
    public void executeMainAccount() {
        log.info("executeMainAccount");
        try (DeliverExecutor deliverExecutor = deliveryFactory.createDeliverExecutor()) {
            deliverExecutor.execute(CustomConfiguration.builder()
                    .userId("mainAccount")
                    .build());
        }
    }
}
