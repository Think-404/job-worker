package org.originit.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Data
@ConfigurationProperties(prefix = "delivery")
@NoArgsConstructor
@AllArgsConstructor
@Component
public class Configuration implements JobSearchConfig, PersonalInfoConfig {


    /**
     * 用于打招呼的语句
     */
    private String introduction;

    /**
     * 技术栈描述, 没有则用sayHi
     */
    private String description;

    /**
     * 搜索关键词列表
     */
    private List<String> keywords;

    /**
     * 城市
     */
    private Set<String> cities;

    /**
     * 行业列表
     */
    private Set<String> industries;

    /**
     * 工作年限 1-10 年, -10 表示 超过 10 年， 0表示应届 -k表示距离毕业k年
     */
    private String workYears;

    /**
     * 工作经验要求
     */
    private List<String> experience;

    /**
     * 工作类型
     */
    private String jobType;

    /**
     * 薪资范围
     */
    private Set<String> salary;

    /**
     * 学历要求列表
     */
    private Set<String> degree;

    /**
     * 公司规模列表
     */
    private Set<String> scale;

    /**
     * 公司融资阶段列表
     */
    private Set<String> stage;

    /**
     * 是否开放AI检测
     */
    private Boolean enableAI;

    /**
     * 薪资下限
     */
    private Integer salaryLeftMin;

    /**
     * 薪资上限
     */
    private Integer salaryRightMax;

    /**
     * 最低薪数
     */
    private Integer minSalaryCount;

    /**
     * 毕业年份
     */
    private Integer graduateYear;

    private Set<String> blackCompanies;

    public String getDescription() {
        if (description == null) {
            return introduction;
        }
        return description;
    }
}
