package org.originit;

import lombok.extern.slf4j.Slf4j;
import org.originit.config.CustomConfiguration;
import org.originit.executor.DeliveryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;

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
        log.info("启动成功, 开始执行任务...");
        context.getBean(DeliveryFactory.class)
                .getDeliverExecutor()
                .execute(CustomConfiguration.builder()
                        .userId("autoTask")
                        .build());
        log.info("任务执行完毕, 退出程序...");

    }

    @Scheduled(cron = "0 15 * * *")
    public void executeNewAccount() {
        log.info("executeNewAccount");
        deliveryFactory.getDeliverExecutor()
                .execute(CustomConfiguration.builder()
                        .userId("newAccount")
                        .build());
    }

    @Scheduled(cron = "0 10 * * *")
    public void executeMainAccount() {
        log.info("executeMainAccount");
        deliveryFactory.getDeliverExecutor()
                .execute(CustomConfiguration.builder()
                        .userId("mainAccount")
                        .build());
    }
}
