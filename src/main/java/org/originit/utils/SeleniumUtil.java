package org.originit.utils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.originit.error.DriverErrorCode;
import org.originit.error.exception.DeliverySystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.originit.utils.Constant.*;

public class SeleniumUtil {
    private static final Logger log = LoggerFactory.getLogger(SeleniumUtil.class);

    public static void initDriver() {
        SeleniumUtil.getChromeDriver();
        SeleniumUtil.getActions();
        SeleniumUtil.getWait();
        SeleniumUtil.getShortWait();
    }

    private static void getShortWait() {
        SHORT_WAIT = new WebDriverWait(CHROME_DRIVER, Duration.of(SHORT_WAIT_TIME, ChronoUnit.SECONDS));
    }

    public static void getChromeDriver() {
        ChromeOptions options = new ChromeOptions();
        // 添加扩展插件
        String osName = System.getProperty("os.name").toLowerCase();
        log.info("当前操作系统为【{}】", osName);
        String osType = getOSType(osName);
        String binary = null;
        switch (osType) {
            case "windows" -> {
                binary = "C:/Program Files/Google/Chrome/Application/chrome.exe";
                System.setProperty("webdriver.chrome.driver", "classpath:chromedriver.exe");
            }
            case "mac" -> {
                binary = "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome";
                System.setProperty("webdriver.chrome.driver", "classpath:chromedriver-mac-arm64/chromedriver");
            }
            case "linux" -> {
                binary = "/usr/bin/google-chrome";
                options.addArguments("--disable-gpu");
                log.info("Disable GPU for Linux");
                System.setProperty("webdriver.chrome.driver", "classpath:chromedriver-linux64/chromedriver");
            }
            default -> {
                log.error("不支持的操作系统【{}】", osName);
                throw new DeliverySystemException(DriverErrorCode.PLATFORM_NOT_SUPPORTED);
            }
        }
        log.info("webdriver.chrome.driver: {}", System.getProperty("webdriver.chrome.driver"));
        log.info("webdriver.chrome.bin: {}", binary);
        options.setBinary(binary);
        handleDriverClassPath();
        options.addExtensions(CommonFileUtils.copyClassPathResource("classpath:xpathHelper.crx", "xpathHelper.crx", false));
        GraphicsDevice[] screens = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
        if (screens.length > 1) {
            options.addArguments("--window-position=2800,1000"); //将窗口移动到副屏的起始位置
        }
//        options.addArguments("--headless"); //使用无头模式
        // 无痕模式
        options.addArguments("--incognito");
        CHROME_DRIVER = new ChromeDriver(options);
        CHROME_DRIVER.manage().window().maximize();
    }

    private static void handleDriverClassPath() {
        String driverPath = System.getProperty("webdriver.chrome.driver");
        File file = CommonFileUtils.copyClassPathResource(driverPath, "chromedriver", false);
        file.setExecutable(true);
        System.setProperty("webdriver.chrome.driver", file.getAbsolutePath());
    }

    public static String getOSType(String osName) {
        if (osName.contains("win")) {
            return "windows";
        }
        if (osName.contains("linux")) {
            return "linux";
        }
        if (osName.contains("mac") || osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
            return "mac";
        }
        return "unknown";
    }

    public static void saveCookie(String path) {
        // 获取所有的cookies
        Set<Cookie> cookies = CHROME_DRIVER.manage().getCookies();
        // 创建一个JSONArray来保存所有的cookie信息
        JSONArray jsonArray = new JSONArray();
        // 将每个cookie转换为一个JSONObject，并添加到JSONArray中
        for (Cookie cookie : cookies) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", cookie.getName());
            jsonObject.put("value", cookie.getValue());
            jsonObject.put("domain", cookie.getDomain());
            jsonObject.put("path", cookie.getPath());
            if (cookie.getExpiry() != null) {
                jsonObject.put("expiry", cookie.getExpiry().getTime());
            }
            jsonObject.put("isSecure", cookie.isSecure());
            jsonObject.put("isHttpOnly", cookie.isHttpOnly());
            jsonArray.put(jsonObject);
        }
        // 将JSONArray写入到一个文件中
        saveCookieToFile(jsonArray, path);
    }

    private static void saveCookieToFile(JSONArray jsonArray, String path) {
        // 将JSONArray写入到一个文件中
        try (FileWriter file = new FileWriter(path)) {
            file.write(jsonArray.toString(4));  // 使用4个空格的缩进
            log.info("Cookie已保存到文件：{}", path);
        } catch (IOException e) {
            log.error("保存cookie异常！保存路径:{}", path);
        }
    }

    private static void updateCookieFile(JSONArray jsonArray, String path) {
        // 将JSONArray写入到一个文件中
        try (FileWriter file = new FileWriter(path)) {
            file.write(jsonArray.toString(4));  // 使用4个空格的缩进
            log.info("cookie文件更新：{}", path);
        } catch (IOException e) {
            log.error("更新cookie异常！保存路径:{}", path);
        }
    }

    public static void loadCookie(String cookiePath) {
        // 首先清除由于浏览器打开已有的cookies
        CHROME_DRIVER.manage().deleteAllCookies();
        // 从文件中读取JSONArray
        JSONArray jsonArray = null;
        try {
            String jsonText = new String(Files.readAllBytes(Paths.get(cookiePath)));
            if (!jsonText.isEmpty()) {
                jsonArray = new JSONArray(jsonText);
            }
        } catch (IOException e) {
            log.error("读取cookie异常！");
        }
        // 遍历JSONArray中的每个JSONObject，并从中获取cookie的信息
        if (jsonArray != null) {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String name = jsonObject.getString("name");
                String value = jsonObject.getString("value");
                String domain = jsonObject.getString("domain");
                String path = jsonObject.getString("path");
                Date expiry = null;
                if (!jsonObject.isNull("expiry")) {
                    expiry = new Date(Instant.now().plus(7, ChronoUnit.DAYS).toEpochMilli());
                    jsonObject.put("expiry", Instant.now().plus(7, ChronoUnit.DAYS).toEpochMilli()); // 更新expiry
                }
                boolean isSecure = jsonObject.getBoolean("isSecure");
                boolean isHttpOnly = jsonObject.getBoolean("isHttpOnly");
                // 使用这些信息来创建新的Cookie对象，并将它们添加到WebDriver中
                Cookie cookie = new Cookie.Builder(name, value)
                        .domain(domain)
                        .path(path)
                        .expiresOn(expiry)
                        .isSecure(isSecure)
                        .isHttpOnly(isHttpOnly)
                        .build();
                try {
                    CHROME_DRIVER.manage().addCookie(cookie);
                } catch (Exception ignore) {
                }
            }
            // 更新cookie文件
            updateCookieFile(jsonArray, cookiePath);
        }
    }

    public static void getActions() {
        ACTIONS = new Actions(Constant.CHROME_DRIVER);
    }

    public static void getWait() {
        WAIT = new WebDriverWait(Constant.CHROME_DRIVER, Duration.ofSeconds(WAIT_TIME));
    }

    public static void sleep(long milliseconds) {
        try {
            log.info("Sleeping for {} milliseconds", milliseconds);
            Thread.sleep(milliseconds / 10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Sleep was interrupted", e);
        }
    }

    public static void sleep(int seconds) {
        sleep(seconds * 1000L);
    }

    public static void sleepByMilliSeconds(int milliSeconds) {
        try {
            TimeUnit.MILLISECONDS.sleep(milliSeconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Sleep was interrupted", e);
        }
    }

    public static List<WebElement> findElements(By by) {
        try {
            return CHROME_DRIVER.findElements(by);
        } catch (Exception e) {
            log.error("Could not find element:{}", by, e);
            return new ArrayList<>();
        }
    }

    public static WebElement findElement(By by) {
        try {
            return CHROME_DRIVER.findElement(by);
        } catch (Exception e) {
            log.error("Could not find element:{}", by, e);
            return null;
        }
    }

    public static void click(By by) {
        try {
            CHROME_DRIVER.findElement(by).click();
        } catch (Exception e) {
            log.error("click element:{}", by, e);
        }
    }

    public static boolean isCookieValid(String cookiePath) {
        return Files.exists(Paths.get(cookiePath));
    }

}
