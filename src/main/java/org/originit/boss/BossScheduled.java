package org.originit.boss;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.originit.utils.JobUtils.formatDuration;
import static org.originit.utils.JobUtils.getDelayTime;

@Slf4j
public class BossScheduled {

    public static void main(String[] args) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
        postJobs();
        scheduleTask(scheduler, BossScheduled::postJobs);
    }

    private static void postJobs() {
    }

    private static void scheduleTask(ScheduledExecutorService scheduler, Runnable task) {
        long delay = getInitialDelay();
        String msg = "【Boss】距离下次投递还有：" + formatDuration(delay);
        log.info(msg);
        scheduler.scheduleAtFixedRate(task, delay, TimeUnit.DAYS.toSeconds(1), TimeUnit.SECONDS);
    }

    private static long getInitialDelay() {
        return getDelayTime();
    }

    private static void safeRun(Runnable task) {
        try {
            task.run();
        } catch (Exception e) {
            log.error("safeRun异常：{}", e.getMessage(), e);
        }
    }
}
