package org.originit.liepin;

import lombok.SneakyThrows;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.originit.utils.JobUtils;
import org.originit.utils.SeleniumUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.originit.utils.Bot.sendMessageByTime;
import static org.originit.utils.Constant.*;
import static org.originit.utils.JobUtils.formatDuration;
import static org.originit.utils.SeleniumUtil.isCookieValid;

public class Liepin {
    private static final Logger log = LoggerFactory.getLogger(Liepin.class);
    static String homeUrl = "https://www.liepin.com/";
    static String cookiePath = "./src/main/java/org.originit.liepin/cookies.json";
    static int maxPage = 50;
    static List<String> resultList = new ArrayList<>();
    static String baseUrl = "https://www.liepin.com/zhaopin/?";
    static LiepinConfig config = LiepinConfig.init();
    static Date startDate;

    public static void main(String[] args) {
        SeleniumUtil.initDriver();
        startDate = new Date();
        login();
        for (String keyword : config.getKeywords()) {
            submit(keyword);
        }
        printResult();
    }

    private static void printResult() {
        String message = String.format("\n猎聘投递完成，共投递%d个岗位，用时%s", resultList.size(), formatDuration(startDate, new Date()));
        log.info(message);
        sendMessageByTime(message);
        resultList.clear();
        CHROME_DRIVER.close();
        CHROME_DRIVER.quit();
    }


    @SneakyThrows
    private static void submit(String keyword) {
        CHROME_DRIVER.get(getSearchUrl() + "&key=" + keyword);
        WAIT.until(ExpectedConditions.presenceOfElementLocated(By.className("list-pagination-box")));
        WebElement div = CHROME_DRIVER.findElement(By.className("list-pagination-box"));
        List<WebElement> lis = div.findElements(By.tagName("li"));
        setMaxPage(lis);
        for (int i = 0; i < maxPage; i++) {
            WAIT.until(ExpectedConditions.presenceOfElementLocated(By.className("subscribe-card-box")));
            log.info("正在投递【{}】第【{}】页...", keyword, i + 1);
            submitJob();
            log.info("已投递第【{}】页所有的岗位...\n", i + 1);
            div = CHROME_DRIVER.findElement(By.className("list-pagination-box"));
            WebElement nextPage = div.findElement(By.xpath(".//li[@title='Next Page']"));
            if (nextPage.getAttribute("disabled") == null) {
                nextPage.click();
            } else {
                break;
            }
        }
        log.info("【{}】关键词投递完成！", keyword);
    }

    private static String getSearchUrl() {
        return baseUrl +
                JobUtils.appendParam("city", config.getCityCode()) +
                JobUtils.appendParam("salary", config.getSalary()) +
                "&currentPage=" + 0 + "&dq=" + config.getCityCode();
    }


    private static void setMaxPage(List<WebElement> lis) {
        try {
            int page = Integer.parseInt(lis.get(lis.size() - 2).getText());
            if (page > 1) {
                maxPage = page;
            }
        } catch (Exception ignored) {
        }
    }

    private static void submitJob() {
        int count = CHROME_DRIVER.findElements(By.cssSelector("div.job-list-box div[style*='margin-bottom']")).size();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            String jobName = CHROME_DRIVER.findElements(By.xpath("//*[contains(@class, 'job-title-box')]")).get(i).getText().replaceAll("\n", " ").replaceAll("【 ", "[").replaceAll(" 】", "]");
            String companyName = CHROME_DRIVER.findElements(By.xpath("//*[contains(@class, 'company-name')]")).get(i).getText().replaceAll("\n", " ");
            String salary = CHROME_DRIVER.findElements(By.xpath("//*[contains(@class, 'job-salary')]")).get(i).getText().replaceAll("\n", " ");
            String recruiterName = null;
            WebElement name;
            try {
                name = CHROME_DRIVER.findElements(By.xpath("//*[contains(@class, 'recruiter-name')]")).get(i);
                recruiterName = name.getText();
            } catch (Exception e) {
                log.error("{}", e.getMessage());
            }
            WebElement title;
            String recruiterTitle = null;
            try {
                title = CHROME_DRIVER.findElements(By.xpath("//*[contains(@class, 'recruiter-title')]")).get(i);
                recruiterTitle = title.getText();
            } catch (Exception e) {
                log.info("【{}】招聘人员:【{}】没有职位描述", companyName, recruiterName);
            }
            try {
                name = CHROME_DRIVER.findElements(By.xpath("//div[@class='jsx-1313209507 recruiter-name ellipsis-1']")).get(i);
                ACTIONS.moveToElement(name).perform();
            } catch (Exception ignore) {
            }
            WebElement button;
            try {
                button = CHROME_DRIVER.findElement(By.xpath("//button[@class='ant-btn ant-btn-primary ant-btn-round']"));
            } catch (Exception e) {
                //嵌套一个异常，用来获取对应按钮
                try {
                    button = CHROME_DRIVER.findElement(By.xpath("//button[@Class='ant-btn ant-btn-round ant-btn-primary']"));
                } catch (Exception e1) {
                    continue;
                }
            }
            String text;
            try {
                text = button.getText();
            } catch (Exception ignore) {
                text = "";
            }
            if (text.contains("聊一聊")) {
                try {
                    button.click();
                } catch (Exception ignore) {
                }
                WAIT.until(ExpectedConditions.presenceOfElementLocated(By.className("__im_basic__header-wrap")));
                WAIT.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//textarea[contains(@class, '__im_basic__textarea')]")));
                WebElement input = CHROME_DRIVER.findElement(By.xpath("//textarea[contains(@class, '__im_basic__textarea')]"));
                input.click();
                SeleniumUtil.sleep(1);
                WebElement close = CHROME_DRIVER.findElement(By.cssSelector("div.__im_basic__contacts-title svg"));
                close.click();
                WAIT.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[contains(@class, 'recruiter-info-box')]")));

                resultList.add(sb.append("【").append(companyName).append(" ").append(jobName).append(" ").append(salary).append(" ").append(recruiterName).append(" ").append(recruiterTitle).append("】").toString());
                sb.setLength(0);
                log.info("发起新聊天:【{}】的【{}·{}】岗位, 【{}:{}】", companyName, jobName, salary, recruiterName, recruiterTitle);
            }
            ACTIONS.moveByOffset(125, 0).perform();
        }
    }

    @SneakyThrows
    private static void login() {
        log.info("正在打开猎聘网站...");
        CHROME_DRIVER.get(homeUrl);
        log.info("猎聘正在登录...");
        if (isCookieValid(cookiePath)) {
            SeleniumUtil.loadCookie(cookiePath);
            CHROME_DRIVER.navigate().refresh();
        }
        WAIT.until(ExpectedConditions.presenceOfElementLocated(By.id("header-logo-box")));
        if (isLoginRequired()) {
            log.info("cookie失效，尝试扫码登录...");
            scanLogin();
            SeleniumUtil.saveCookie(cookiePath);
        } else {
            log.info("cookie有效，准备投递...");
        }
    }

    private static boolean isLoginRequired() {
        String currentUrl = CHROME_DRIVER.getCurrentUrl();
        return !currentUrl.contains("c.org.originit.liepin.com");
    }

    private static void scanLogin() {
        try {
            SeleniumUtil.click(By.className("switch-login-type-btn-box"));
            log.info("等待扫码..");
            boolean isLoggedIn = false;

            // 一直循环，直到元素出现（用户扫码登录成功）
            while (!isLoggedIn) {
                try {
                    isLoggedIn = !CHROME_DRIVER.findElements(By.xpath("//*[@id=\"main-container\"]/div/div[3]/div[2]/div[3]/div[1]/div[1]")).isEmpty();
                } catch (Exception ignored) {
                    SeleniumUtil.sleep(1);
                }
            }
            log.info("用户扫码成功，继续执行...");
        } catch (Exception e) {
            log.error("scanLogin() 失败: {}", e.getMessage());
        }
    }


}
