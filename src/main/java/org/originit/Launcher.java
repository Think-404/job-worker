package org.originit;

import lombok.extern.slf4j.Slf4j;
import org.originit.config.CustomConfiguration;
import org.originit.executor.DeliverExecutor;
import org.originit.executor.DeliveryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.CountDownLatch;

@SpringBootApplication
@Slf4j
public class Launcher {

    @Autowired
    private DeliveryFactory deliveryFactory;

    @Value("${delivery.execute:false}")
    private boolean execute;

    public static void main(String[] args) {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(Launcher.class);
        //不启动web服务器
        builder.web(WebApplicationType.NONE);
        builder.allowCircularReferences(true);
        SpringApplication app = builder.build();
        ConfigurableApplicationContext context = app.run(args);
        if (context.getBean(Launcher.class).execute) {
            execute(context);
        } else {
            check(context);
        }
    }

    private static void execute(ApplicationContext context) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        new Thread(() -> {
            try (DeliverExecutor deliverExecutor = context.getBean(DeliveryFactory.class)
                    .createDeliverExecutor()) {
                deliverExecutor.execute(CustomConfiguration.builder()
                        .userId("mainAccount")
                        .build());
            }
        }).start();
        // try (DeliverExecutor deliverExecutor = context.getBean(DeliveryFactory.class)
        //         .createDeliverExecutor()) {
        //     deliverExecutor.execute(CustomConfiguration.builder()
        //             .userId("newAccount")
        //             .build());
        // }
        // try {
        //     countDownLatch.await();
        // } catch (InterruptedException e) {
        //     log.error("停止运行", e);
        // }
    }


    private static void check(ApplicationContext context) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        new Thread(() -> {
            try (DeliverExecutor deliverExecutor = context.getBean(DeliveryFactory.class)
                    .createDeliverExecutor()) {
                deliverExecutor.tryLogin(CustomConfiguration.builder()
                        .userId("mainAccount")
                        .build());
            }
        }).start();
        try (DeliverExecutor deliverExecutor = context.getBean(DeliveryFactory.class)
                .createDeliverExecutor()) {
            deliverExecutor.tryLogin(CustomConfiguration.builder()
                    .userId("newAccount")
                    .build());
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            log.error("停止运行", e);
        }
    }

    @Scheduled(cron = "0 11 * * *")
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
