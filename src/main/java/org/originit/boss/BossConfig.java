package org.originit.boss;

import lombok.Data;
import lombok.SneakyThrows;
import org.originit.utils.JobUtils;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class BossConfig {
    /**
     * 用于打招呼的语句
     */
    private String sayHi;

    /**
     * 搜索关键词列表
     */
    private List<String> keywords;

    /**
     * 城市编码
     */
    private List<String> cityCode;

    /**
     * 行业列表
     */
    private List<String> industry;

    /**
     * 工作年限
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
    private List<String> salarys;

    /**
     * 学历要求列表
     */
    private List<String> degree;

    /**
     * 公司规模列表
     */
    private List<String> scale;

    /**
     * 公司融资阶段列表
     */
    private List<String> stage;

    /**
     * 是否开放AI检测
     */
    private Boolean enableAI;

    /**
     * 技术栈描述
     */
    private Boolean aiDescribe;

    private Integer salaryLeftMin;

    private Integer salaryRightMax;

    private Integer minSalaryCount;

    private Integer graduateYear;

    @SneakyThrows
    public static BossConfig init() {
        BossConfig config = JobUtils.getConfig(BossConfig.class);

        // 转换工作类型
        config.setJobType(BossEnum.JobType.forValue(config.getJobType()).getCode());
        // 转换薪资范围
        config.setSalarys(config.getSalarys().stream().map(item -> BossEnum.Salary.forValue(item).getCode()).toList());
        // 转换城市编码
        config.setCityCode(config.getCityCode().stream().map(value -> BossEnum.CityCode.forValue(value).getCode()).collect(Collectors.toList()));
        // 转换工作经验要求
        config.setExperience(config.getExperience().stream().map(value -> BossEnum.Experience.forValue(value).getCode()).collect(Collectors.toList()));
        // 转换学历要求
        config.setDegree(config.getDegree().stream().map(value -> BossEnum.Degree.forValue(value).getCode()).collect(Collectors.toList()));
        // 转换公司规模
        config.setScale(config.getScale().stream().map(value -> BossEnum.Scale.forValue(value).getCode()).collect(Collectors.toList()));
        // 转换公司融资阶段
        config.setStage(config.getStage().stream().map(value -> BossEnum.Financing.forValue(value).getCode()).collect(Collectors.toList()));
        // 转换行业
        config.setIndustry(config.getIndustry().stream().map(value -> BossEnum.Industry.forValue(value).getCode()).collect(Collectors.toList()));

        return config;
    }

}

