package utils;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

@Data
@Slf4j
public class Job implements Serializable {
    public static final String GRADUATE_TIME = "毕业时间:";
    /**
     * 岗位链接
     */
    private String href;

    /**
     * 岗位名称
     */
    private String jobName;

    /**
     * 岗位地区
     */
    private String jobArea;

    /**
     * 岗位信息
     */
    private String jobInfo;

    /**
     * 岗位薪水
     */
    private String salary;

    /**
     * 公司标签
     */
    private String companyTag;

    /**
     * HR名称
     */
    private String recruiter;

    /**
     * 公司名字
     */
    private String companyName;

    /**
     * 公司信息
     */
    private String companyInfo;

    /**
     * 招聘要求几几届的学生
     */
    private String graduateInfo;

    private String recruitTime;

    public Integer getGraduateYear() {
        String graduateYearText = graduateInfo;
        if (graduateInfo == null) {
            return null;
        }
        if (graduateYearText.startsWith("毕业时间：不限")) {
            log.info("【毕业时间不限】{}", graduateYearText);
            return null;
        }
        // 正则匹配: 毕业时间:数字年
        if (graduateYearText.length() <= GRADUATE_TIME.length()) {
            log.error("【毕业时间格式错误】{}", graduateYearText);
            return null;
        }
        String graduateYear = graduateYearText.substring(GRADUATE_TIME.length()).trim();
        try {
            return Integer.parseInt(graduateYear.substring(0, 4));
        } catch (NumberFormatException e) {
            log.error("【毕业时间格式错误】{}", graduateYearText);
            return null;
        }
    }

}


