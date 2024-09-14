package org.originit.boss;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.RandomUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.originit.config.AIConfig;
import org.originit.config.Configuration;
import org.originit.config.CustomConfiguration;
import org.originit.executor.DeliverExecutor;
import org.originit.filter.Filters;
import org.originit.manager.CookieManager;
import org.originit.model.FilterContext;
import org.originit.service.BotService;
import org.originit.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.originit.utils.JobUtils.formatDuration;

/**
 * @author loks666
 * Boss直聘自动投递
 */
@RequiredArgsConstructor
public class Boss implements DeliverExecutor {
    public static final int NO_JOB_MAX_PAGE = 5; // 无岗位最大页数
    public static final String HOME_URL = "https://www.zhipin.com";
    public static final String baseUrl = "https://www.zhipin.com/web/geek/job?";
    private static final Logger log = LoggerFactory.getLogger(Boss.class);
    private Integer page = 1;
    private final List<Job> resultList = new ArrayList<>();
    private String cookiePath;
    private int noJobPages;

    private final Configuration configuration;

    private final AIConfig aiConfig;

    private final ChromeDriver webDriver;

    private final CookieManager cookieManager;

    private final Filters filters;

    private final BotService botService;

    private Wait<WebDriver> wait;

    private Wait<WebDriver> shortWait;

    private Date startDate;

    private FilterContext filterContext;

    @Override
    public void execute(CustomConfiguration customConfiguration) {
        cookiePath = CommonFileUtils.getRunDirFile(customConfiguration.getUserId() + File.pathSeparator + "cookies.json").getAbsolutePath();
        wait = new WebDriverWait(webDriver, Duration.ofSeconds(Constant.WAIT_TIME));
        shortWait = new WebDriverWait(webDriver, Duration.ofSeconds(Constant.SHORT_WAIT_TIME));
        startDate = new Date();
        log.info("Boss直聘自动投递开始");
        log.info("配置信息：{}", configuration);
        login();
        filterContext = FilterContext.builder()
                .configuration(configuration)
                .aiConfig(aiConfig)
                .graduateYear(configuration.getGraduateYear())
                .salaryRightMax(configuration.getSalaryRightMax())
                .salaryLeftMin(configuration.getSalaryLeftMin())
                .build();
        for (String salary : configuration.getSalary()) {
            for (String city : configuration.getCities()) {
                BossEnum.CityCode cityCode = BossEnum.CityCode.forValue(city);
                if (cityCode == null) {
                    log.error("未找到城市【{}】, 跳过", city);
                    continue;
                }
                BossEnum.Salary salaryCode = BossEnum.Salary.forValue(salary);
                if (salaryCode == null) {
                    log.error("未找到薪资【{}】, 跳过", salary);
                    continue;
                }
                postJob(cityCode.getCode(), salaryCode.getCode());
            }
        }
        log.info(resultList.isEmpty() ? "未发起新的聊天..." : "新发起聊天公司如下:\n{}", resultList.stream().map(Object::toString).collect(Collectors.joining("\n")));
        printResult();
    }

    private void printResult() {
        String message = String.format("\nBoss投递完成，共发起%d个聊天，用时%s", resultList.size(), formatDuration(startDate, new Date()));
        log.info(message);
        botService.sendMessageWithDateTime(message);
    }

    private void postJob(String cityCode, String salary) {
        String searchUrl = getSearchUrl(cityCode, salary);
        log.info("开始投递【{}】", searchUrl);
        endSubmission:
        for (String keyword : configuration.getKeywords()) {
            page = 1;
            noJobPages = 0;
            int failLimit = 5;
            while (true) {
                log.info("投递【{}】关键词第【{}】页", keyword, page);
                String url = searchUrl + "&page=" + page;
                int startSize = resultList.size();
                Integer resultSize;
                long now = System.currentTimeMillis();
                try {
                    resultSize = resumeSubmission(url, keyword);
                } catch (Exception e) {
                    log.error("投递【{}】关键词第【{}】页失败", keyword, page, e);
                    page++;
                    failLimit--;
                    if (failLimit <= 0) {
                        log.error("失败【{}】次，结束该关键词的投递...", 5);
                        break;
                    }
                    continue;
                } finally {
                    // 防止频繁访问随机休眠，至少3s访问一个页面
                    if (System.currentTimeMillis() - now < 3000) {
                        ThreadUtil.sleep(3000 + now - System.currentTimeMillis() + RandomUtil.randomInt(100, 2000));
                    }
                }
                if (resultSize == -1) {
                    log.info("今日沟通人数已达上限，请明天再试");
                    botService.sendMessage("今日沟通人数已达上限，请明天再试");
                    throw new RuntimeException("今日沟通人数已达上限，请明天再试");
                }
                if (resultSize == -2) {
                    log.info("出现异常访问，请手动过验证后再继续投递...");
                    botService.sendMessage("出现异常访问，请手动过验证后再继续投递...");
                    break endSubmission;
                }
                if (resultSize == startSize) {
                    noJobPages++;
                    if (noJobPages >= NO_JOB_MAX_PAGE) {
                        log.info("【{}】关键词已经连续【{}】页无岗位，结束该关键词的投递...", keyword, noJobPages);
                        break;
                    } else {
                        log.info("【{}】关键词第【{}】页无岗位,目前已连续【{}】页无新岗位...", keyword, page, noJobPages);
                    }
                } else {
                    noJobPages = 0;
                }
                page++;
            }
            ThreadUtil.sleep(1000);
        }
    }

    private String getSearchUrl(String cityCode, String salary) {
        return baseUrl +
                JobUtils.appendParam("city", cityCode) +
                JobUtils.appendParam("jobType", configuration.getJobType(), jobType -> {
                    return BossEnum.JobType.forValue(jobType).getCode();
                }) +
                JobUtils.appendParam("salary", salary) +
                JobUtils.appendListParam("experience", configuration.getExperience(), e -> {
                    return BossEnum.Experience.forValue(e).getCode();
                }) +
                JobUtils.appendListParam("degree", configuration.getDegree(), d -> {
                    return BossEnum.Degree.forValue(d).getCode();
                }) +
                JobUtils.appendListParam("scale", configuration.getScale(), s -> {
                    return BossEnum.Scale.forValue(s).getCode();
                }) +
                JobUtils.appendListParam("stage", configuration.getStage(), s -> {
                    return BossEnum.Financing.forValue(s).getCode();
                });
    }

    @SneakyThrows
    private Integer resumeSubmission(String url, String keyword) {
        webDriver.get(url + "&query=" + keyword);
        log.info("打开【{}】关键词第【{}】页...", keyword, page);
        RetryUtil.retry(() -> {
            log.info("等待页面加载...");
            WebElement until = shortWait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.job-title.clearfix")));
            if (until != null) {
                return until;
            }
            // 获取当前浏览器的url
            String currentUrl = webDriver.getCurrentUrl();
            if (currentUrl.contains("security-check")) {
                log.warn("出现安全检查，刷新页面...");
                webDriver.get(url + "&query=" + keyword);
                ThreadUtil.sleep(1000);
            }
            return null;
        }, 10);
        List<WebElement> jobCards = webDriver.findElements(By.cssSelector("li.job-card-wrapper"));
        log.info("【{}】关键词第【{}】页共【{}】个岗位", keyword, page, jobCards.size());
         List<Job> jobs = new ArrayList<>();
         // 这里是列表页，jobCards是列表页中的所有工作的卡片的dom
        for (WebElement jobCard : jobCards) {
            boolean isHeadHunting = isHeadHunting(jobCard);
            WebElement infoPublic = jobCard.findElement(By.cssSelector("div.info-public"));
            String recruiterText = jobCard.getText();
            String recruiterName = infoPublic.findElement(By.cssSelector("em")).getText();
            String jobName = jobCard.findElement(By.cssSelector("div.job-title span.job-name")).getText();
            String companyName = jobCard.findElement(By.cssSelector("div.company-info h3.company-name")).getText();
            // 转换出job对象
            Job job = new Job();
            job.setTags(new ArrayList<>());
            if (isHeadHunting) {
                job.getTags().add("猎头");
            }
            job.setCompanyName(companyName);
            job.setRecruiter(recruiterText.replace(recruiterName, "") + ":" + recruiterName);
            job.setHref(jobCard.findElement(By.cssSelector("a")).getAttribute("href"));
            job.setJobName(jobName);
            job.setJobArea(jobCard.findElement(By.cssSelector("div.job-title span.job-area")).getText());
            job.setSalary(jobCard.findElement(By.cssSelector("div.job-info span.salary")).getText());
            List<WebElement> tagElements = jobCard.findElements(By.cssSelector("div.job-info ul.tag-list li"));
            StringBuilder tag = new StringBuilder();
            for (WebElement tagElement : tagElements) {
                tag.append(tagElement.getText()).append("·");
            }
            job.setCompanyTag(tag.substring(0, tag.length() - 1));
            jobs.add(job);
        }
        for (Job job : jobs) {
            // 一开始先过滤一次，就不用进详情页了
            if (!filters.doFilter(filterContext, job)) {
                log.info("【{}】不符合过滤条件，跳过...", job.getJobName());
                continue;
            }
            // 打开新的标签页并打开链接，进【详情页】
            ((JavascriptExecutor) webDriver).executeScript("window.open(arguments[0], '_blank')", job.getHref());
            // 切换到新的标签页
            ArrayList<String> tabs = new ArrayList<>(webDriver.getWindowHandles());
            try {
                webDriver.switchTo().window(tabs.get(tabs.size() - 1));
                try {
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[class*='btn btn-startchat']")));
                } catch (Exception e) {
                    WebElement element = SeleniumUtil.findElement(By.xpath("//div[@class='error-content']"));
                    if (element != null && element.getText().contains("异常访问")) {
                        return -2;
                    }
                }
                // 随机等待一段时间
                SeleniumUtil.sleep(JobUtils.getRandomNumberInRange(3, 10));
                WebElement btn = webDriver.findElement(By.cssSelector("[class*='btn btn-startchat']"));
                // 再次获取更详细的信息
                job.setJobInfo(webDriver.findElement(By.xpath("//div[@class='job-sec-text']")).getText());
                WebElement experienceText = webDriver.findElement(By.cssSelector("[class*='text-desc text-experiece']"));
                job.setCompanyTag(experienceText.getText().trim());
                dealSchoolRecruitment(job);
                // 再次过滤一遍，防止投递不合要求的工作
                if (!filters.doFilter(filterContext, job)) {
                    log.info("【{}】不符合过滤条件，跳过...", job.getJobName());
                    continue;
                }
                if ("立即沟通".equals(btn.getText())) {
                    btn.click();
                    if (isLimit()) {
                        SeleniumUtil.sleep(1);
                        return -1;
                    }
                    try {
                        SeleniumUtil.sleep(1);
                        try {
                            webDriver.findElement(By.xpath("//textarea[@class='input-area']"));
                            WebElement close = webDriver.findElement(By.xpath("//i[@class='icon-close']"));
                            close.click();
                            btn.click();
                        } catch (Exception ignore) {
                        }
                        WebElement input = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@id='chat-input']")));
                        input.click();
                        SeleniumUtil.sleep(1);
                        WebElement element = webDriver.findElement(By.xpath("//div[@class='dialog-container']"));
                        if ("不匹配".equals(element.getText())) {
                            webDriver.close();
                            webDriver.switchTo().window(tabs.get(0));
                            continue;
                        }
                        input.sendKeys(configuration.getIntroduction());
                        WebElement send = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//button[@type='send']")));
                        send.click();

                        WebElement recruiterNameElement = webDriver.findElement(By.xpath("//p[@class='base-info fl']/span[@class='name']"));
                        WebElement recruiterTitleElement = webDriver.findElement(By.xpath("//p[@class='base-info fl']/span[@class='base-title']"));
                        String recruiter = recruiterNameElement.getText() + " " + recruiterTitleElement.getText();

                        WebElement companyElement;
                        try {
                            companyElement = webDriver.findElement(By.xpath("//p[@class='base-info fl']/span[not(@class)]"));
                        } catch (Exception e) {
                            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//p[@class='base-info fl']/span[not(@class)]")));
                            companyElement = webDriver.findElement(By.xpath("//p[@class='base-info fl']/span[not(@class)]"));

                        }
                        String company = null;
                        if (companyElement != null) {
                            company = companyElement.getText();
                            job.setCompanyName(company);
                        }
                        WebElement positionNameElement = webDriver.findElement(By.xpath("//a[@class='position-content']/span[@class='position-name']"));
                        WebElement salaryElement = webDriver.findElement(By.xpath("//a[@class='position-content']/span[@class='salary']"));
                        WebElement cityElement = webDriver.findElement(By.xpath("//a[@class='position-content']/span[@class='city']"));
                        String position = positionNameElement.getText() + " " + salaryElement.getText() + " " + cityElement.getText();
                        log.info("投递【{}】公司，【{}】职位，招聘官:【{}】", company == null ? "未知公司: " + job.getHref() : company, position, recruiter);
                        botService.sendMessage("投递【%s】公司，【%s】职位，招聘官:【%s】".formatted(company == null ? "未知公司: " + job.getHref() : company, position, recruiter));
                        resultList.add(job);
                        noJobPages = 0;
                    } catch (Exception e) {
                        log.error("发送消息失败:{}", e.getMessage(), e);
                    }
                }
            } finally {
                SeleniumUtil.sleep(1);
                webDriver.close();
                webDriver.switchTo().window(tabs.get(0));
            }

        }
        return resultList.size();
    }

    private static boolean isHeadHunting(WebElement jobCard) {
        boolean isHeadHunting;
        // 這裏是為了判斷是否是獵頭職位，但是可能找不到標簽，所以用try-catch，抓所以異常，直接返回不是獵頭
        try {
            WebElement tagEle = jobCard.findElement(By.cssSelector(".job-tag-icon"));
            isHeadHunting = tagEle != null && tagEle.getAttribute("alt").contains("猎头");
        } catch (Exception e) {
            return false;
        }
        return isHeadHunting;
    }


    private void dealSchoolRecruitment(Job job) {
        try {
            WebElement schoolJobInfo = webDriver.findElement(By.cssSelector("[class*='school-job-sec']"));
            String[] values = schoolJobInfo.getText().split(" ");
            job.setGraduateInfo(values[0]);
            job.setRecruitTime(values[1]);
            log.info("【{}】是校招职位，毕业时间：【{}】，招聘时间：【{}】", job.getJobName(), values[0], values[1]);
        } catch (Exception e) {
            log.info("【{}】不是校招职位", job.getJobName());
        }
    }

    private static boolean isTargetJob(String keyword, String jobName) {
        boolean keywordIsAI = false;
        for (String target : new String[]{"大模型", "AI"}) {
            if (keyword.contains(target)) {
                keywordIsAI = true;
                break;
            }
        }

        boolean jobIsDesign = false;
        for (String designOrVision : new String[]{"设计", "视觉", "产品", "运营"}) {
            if (jobName.contains(designOrVision)) {
                jobIsDesign = true;
                break;
            }
        }

        boolean jobIsAI = false;
        for (String target : new String[]{"AI", "人工智能", "大模型", "生成"}) {
            if (jobName.contains(target)) {
                jobIsAI = true;
                break;
            }
        }

        if (keywordIsAI) {
            if (jobIsDesign) {
                return false;
            } else if (!jobIsAI) {
                return true;
            }
        }
        return true;
    }


    private boolean isLimit() {
        try {
            SeleniumUtil.sleep(1);
            String text = webDriver.findElement(By.className("dialog-con")).getText();
            return text.contains("已达上限");
        } catch (Exception e) {
            return false;
        }
    }

    @SneakyThrows
    private void login() {
        log.info("打开Boss直聘网站中...");
        webDriver.get(HOME_URL);
        if (SeleniumUtil.isCookieValid(cookiePath)) {
            cookieManager.loadCookies(webDriver, cookiePath);
            webDriver.navigate().refresh();
            SeleniumUtil.sleep(2);
        }
        webDriver.get(HOME_URL);
        if (isLoginRequired()) {
            log.error("cookie失效，尝试扫码登录...");
            scanLogin();
        }
    }


    private boolean isLoginRequired() {
        try {
            String text = webDriver.findElement(By.className("btns")).getText();
            return text != null && text.contains("登录");
        } catch (Exception e) {
            log.info("cookie有效，已登录...");
            return false;
        }
    }

    @SneakyThrows
    private void scanLogin() {
        webDriver.get(HOME_URL + "/web/user/?ka=header-login");
        log.info("等待登陆..");
        WebElement app = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='btn-sign-switch ewm-switch']")));
        boolean login = false;
        while (!login) {
            try {
                app.click();
                wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"header\"]/div[1]/div[1]/a")));
                wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"wrap\"]/div[2]/div[1]/div/div[1]/a[2]")));
                login = true;
                log.info("登录成功！保存cookie...");
            } catch (Exception e) {
                log.error("登陆失败，两秒后重试...");
            } finally {
                SeleniumUtil.sleep(2);
            }
        }
        cookieManager.saveCookies(webDriver, cookiePath);
    }


    @Override
    public void close() {
        webDriver.close();
        webDriver.quit();
    }
}

