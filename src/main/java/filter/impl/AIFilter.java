package filter.impl;

import ai.AiConfig;
import ai.AiService;
import boss.BossConfig;
import cn.hutool.core.date.DateUtil;
import filter.Filter;
import lombok.extern.slf4j.Slf4j;
import utils.Bot;
import utils.Job;

import java.util.Objects;

@Slf4j
public class AIFilter implements Filter {

    BossConfig bossConfig = BossConfig.init();
    @Override
    public boolean doFilter(Job job) {
        if (bossConfig.getEnableAI() == null || !bossConfig.getEnableAI()) {
            return true;
        }
        if (job.getJobInfo() == null) {
            return true;
        }
        StringBuilder requestMessage = new StringBuilder();
        if (bossConfig.getKeywords() != null && !bossConfig.getKeywords().isEmpty()) {
            requestMessage.append(",我对【").append(String.join(",", bossConfig.getKeywords())).append("】比较感兴趣");
        }
        if (bossConfig.getGraduateYear() != null) {
            requestMessage.append(",我是").append(bossConfig.getGraduateYear()).append("届毕业生");
        }
        if (bossConfig.getWorkYears() != null) {
            requestMessage.append(",我的工作经验为【").append(bossConfig.getWorkYears()).append("】,跟目标工作要求差距较大则不符合.");
        }
        if (bossConfig.getMinSalaryCount() != null) {
            requestMessage.append(",我的期望薪次是【").append(bossConfig.getMinSalaryCount()).append("】");
        }
        if (bossConfig.getSalaryLeftMin() != null) {
            requestMessage.append("我的最低薪资要求是【").append(bossConfig.getSalaryLeftMin()).append("】");
        }
        if (bossConfig.getSalaryRightMax() != null) {
            requestMessage.append("我的最高薪资要求是【").append(bossConfig.getSalaryRightMax()).append("】");
        }
        requestMessage.append("今天是").append(DateUtil.now()).append("招聘时间不在范围内则不符合.");
        requestMessage.append("如果上述条件与下面的工作要求不一致，则返回false.");
        String jobInfo = job.getJobInfo();
        // 替换掉空白字符
        requestMessage.append("工作【").append(job.getJobName()).append("】,")
                .append("要求【").append(cleanString(jobInfo)).append("】.");
        if (bossConfig.getSayHi() != null) {
            requestMessage.append("我的介绍:").append(bossConfig.getSayHi())
                .append(",如果这个岗位和我的期望与经历基本符合,")
                .append("注意是基本符合，请返回true,否则返回false.");
        }
        log.info("" +
            ": {}", requestMessage);
        String result = AiService.sendRequest(requestMessage.toString());
        boolean res = result.contains("true");
        if (!res) {
            log.error("AI Filter failed, job: {}, result: {}", job, result);
        }
        log.info("AI Filter result: {}", res);
        return res;
    }

    public static String cleanString(String input) {
        StringBuilder result = new StringBuilder();

        // 定义要删除的中文和英文符号
        String chinesePunctuation = "。？，、；：“”‘’（）【】《》——……￥";
        String englishPunctuation = ".,;:?\"'()[]<>-_+=&*#@%^$`~!";

        for (char c : input.toCharArray()) {
            // 跳过空白字符
            if (Character.isWhitespace(c)) {
                continue;
            }

            // 跳过中文符号
            if (chinesePunctuation.indexOf(c) != -1) {
                continue;
            }

            // 跳过英文符号
            if (englishPunctuation.indexOf(c) != -1) {
                continue;
            }

            // 如果不是空白字符，也不是中英文符号，则添加到结果中
            result.append(c);
        }

        return result.toString();
    }
}
