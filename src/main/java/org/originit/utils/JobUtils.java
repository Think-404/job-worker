package org.originit.utils;

import lombok.SneakyThrows;

import java.util.*;
import java.util.function.Function;

import static org.originit.utils.Constant.UNLIMITED_CODE;

public class JobUtils {

    public static String appendParam(String name, String value) {
        return appendParam(name, value, Function.identity());
    }

    public static String appendParam(String name, String value, Function<String, String> mapper) {
        value = mapper.apply(value);
        return Optional.ofNullable(value)
                .filter(v -> !Objects.equals(UNLIMITED_CODE, v))
                .map(v -> "&" + name + "=" + v)
                .orElse("");
    }

    public static String appendListParam(String name, Collection<String> values) {
        return appendListParam(name, values, Function.identity());
    }

    public static String appendListParam(String name, Collection<String> values, Function<String, String> mapper) {
        return Optional.ofNullable(values)
                .map(list -> list.stream().map(mapper).toList())
                .filter(list -> !list.isEmpty() && !Objects.equals(UNLIMITED_CODE, list.get(0)))
                .map(list -> "&" + name + "=" + String.join(",", list))
                .orElse("");
    }

    @SneakyThrows
    public static <T> T getConfig(Class<T> clazz) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * 计算并格式化时间（毫秒）
     *
     * @param startDate 开始时间
     * @param endDate   结束时间
     * @return 格式化后的时间字符串，格式为 "HH:mm:ss"
     */
    public static String formatDuration(Date startDate, Date endDate) {
        long durationMillis = endDate.getTime() - startDate.getTime();
        long seconds = (durationMillis / 1000) % 60;
        long minutes = (durationMillis / (1000 * 60)) % 60;
        long hours = durationMillis / (1000 * 60 * 60);
        return String.format("%d时%d分%d秒", hours, minutes, seconds);
    }

    /**
     * 将给定的毫秒时间戳转换为格式化的时间字符串
     *
     * @param durationSeconds 持续时间的时间戳（秒）
     * @return 格式化后的时间字符串，格式为 "HH:mm:ss"
     */
    public static String formatDuration(long durationSeconds) {
        long seconds = durationSeconds % 60;
        long minutes = (durationSeconds / 60) % 60;
        long hours = durationSeconds / 3600; // 直接计算总小时数

        return String.format("%d时%d分%d秒", hours, minutes, seconds);
    }

    public static long getDelayTime() {
        Calendar nextRun = Calendar.getInstance();
        // 先将时间调整为明天
        nextRun.add(Calendar.DAY_OF_YEAR, 1); // 加一天
        // 设置时间为8点
        nextRun.set(Calendar.HOUR_OF_DAY, 8);
        nextRun.set(Calendar.MINUTE, 0);
        nextRun.set(Calendar.SECOND, 0);
        nextRun.set(Calendar.MILLISECOND, 0);
        long currentTime = System.currentTimeMillis();
        return (nextRun.getTimeInMillis() - currentTime) / 1000; // 返回秒数
    }

    public static int getRandomNumberInRange(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("max must be greater than or equal to min");
        }
        Random random = new Random();
        return random.nextInt((max - min) + 1) + min;
    }

    public static void main(String[] args) {
        Date star = new Date();
        SeleniumUtil.sleep(3);
        String a = formatDuration(star, new Date());
        System.out.println(a);
    }
}