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
        if (bossConfig.getSayHi() != null) {
            requestMessage.append(bossConfig.getSayHi());
        }
        if (bossConfig.getKeywords() != null && !bossConfig.getKeywords().isEmpty()) {
            requestMessage.append(",我对【").append(bossConfig.getKeywords().get(0)).append("】比较感兴趣");
        }
        if (bossConfig.getGraduateYear() != null) {
            requestMessage.append(",我是").append(bossConfig.getGraduateYear()).append("届毕业生");
        }
        if (bossConfig.getWorkYears() != null) {
            requestMessage.append(",我的工作经验为【").append(bossConfig.getWorkYears()).append("】,跟目标工作差距不大就行");
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
        String jobInfo = job.getJobInfo();
        // 替换掉空白字符
        jobInfo = jobInfo.replaceAll(" +"," ");
        jobInfo = jobInfo.replaceAll("\\s*", "");
        // 替换标点符号
        jobInfo = jobInfo.replaceAll("[\\pP\\p{Punct}]", "");
        jobInfo = jobInfo.substring(0, Math.min(jobInfo.length(), 200));
        requestMessage.append("工作【").append(job.getJobName()).append("】")
                .append("要求【").append(jobInfo).append("】")
                .append(",如果这个岗位和我的期望与经历基本符合，")
                .append("注意是基本符合，请返回true, 如果这个岗位和我的期望经历完全不相干，返回原因");
        String result = AiService.sendRequest(requestMessage.toString());
        boolean res = result.contains("true");
        if (!res) {
            log.error("AI Filter failed, job: {}, result: {}", job, result);
        }
        log.info("AI Filter result: {}", res);
        return res;
    }
}
