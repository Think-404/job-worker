package org.originit.config;

import java.util.List;
import java.util.Set;

public interface JobSearchConfig {

    /**
     * 搜索关键词列表
     */
    List<String> getKeywords();

    /**
     * 城市
     */
    Set<String> getCities();

    /**
     * 行业列表
     */
    Set<String> getIndustries();

    /**
     * 工作年限
     */
    String getWorkYears();

    /**
     * 工作经验要求
     */
    List<String> getExperience();

    /**
     * 工作类型
     */
    String getJobType();

    /**
     * 薪资范围
     */
    Set<String> getSalary();

    /**
     * 学历要求列表
     */
    Set<String> getDegree();

    /**
     * 公司规模列表
     */
    Set<String> getScale();

    /**
     * 公司融资阶段列表
     */
    Set<String> getStage();

    Set<String> getBlackCompanies();

}
