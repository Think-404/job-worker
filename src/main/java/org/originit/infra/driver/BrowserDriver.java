package org.originit.infra.driver;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.originit.error.DriverErrorCode;
import org.originit.error.exception.DeliverySystemException;
import org.originit.utils.CommonFileUtils;
import org.originit.utils.SeleniumUtil;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@Slf4j
public class BrowserDriver implements ChromeDriverManager {

    private ChromeOptions getChromeOptions() {
        ChromeOptions options = new ChromeOptions();
        // 添加扩展插件
        String osName = System.getProperty("os.name").toLowerCase();
        log.info("当前操作系统为【{}】", osName);
        String osType = SeleniumUtil.getOSType(osName);
        String binary;
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
//        GraphicsDevice[] screens = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
//        if (screens != null && screens.length > 1) {
//            options.addArguments("--window-position=2800,1000"); //将窗口移动到副屏的起始位置
//        }
//        options.addArguments("--headless"); //使用无头模式
        // 无痕模式
        options.addArguments("--incognito");
        return options;
    }

    private static void handleDriverClassPath() {
        String driverPath = System.getProperty("webdriver.chrome.driver");
        File file = CommonFileUtils.copyClassPathResource(driverPath, "chromedriver", false);
        file.setExecutable(true);
        System.setProperty("webdriver.chrome.driver", file.getAbsolutePath());
    }

    @Override
    public ChromeDriver createDriver() {
        ChromeDriver driver = new ChromeDriver(getChromeOptions());
        driver.manage().window().maximize();
        return driver;
    }
}
