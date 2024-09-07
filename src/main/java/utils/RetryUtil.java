package utils;

import java.util.NoSuchElementException;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RetryUtil {

    public static <T> T retry(Supplier<T> task, int times) {
        for (int i = 0; i < times; i++) {
            try {
                T t = task.get();
                if (t != null) {
                    return t;
                }
                log.warn("重试次数: {}, 任务返回值为空", i + 1);
            } catch (NoSuchElementException e) {
                if (i == times - 1) {
                    throw e;
                }
            } catch (Exception e) {
                log.error("重试异常, 重试次数: {}, 异常信息: {}", i + 1, e.getMessage(), e);
                if (i == times - 1) {
                    throw e;
                }
            }
        }
        throw new IllegalStateException("重试失败, 重试次数: " + times);
    }
}
