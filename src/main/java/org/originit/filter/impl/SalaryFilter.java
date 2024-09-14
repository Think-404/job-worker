package org.originit.filter.impl;

import lombok.extern.slf4j.Slf4j;
import org.originit.filter.Filter;
import org.originit.model.FilterContext;
import org.originit.utils.Job;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SalaryFilter implements Filter {

    @Override
    public boolean doFilter(FilterContext filterContext, Job job) {
        Integer salaryLeftMin = filterContext.getConfiguration().getSalaryLeftMin();
        Integer salaryRightMax = filterContext.getConfiguration().getSalaryRightMax();
        Integer minSalaryCount = filterContext.getConfiguration().getMinSalaryCount();
        if (job.getSalary() == null) {
            return true;
        }
        String salary = job.getSalary().trim();
        if (!salary.contains("-")) {
            return true;
        }
        try {
            String[] split = salary.split("K")[0].split("-");
            // 过滤月薪
            if ((salaryLeftMin != null && salaryLeftMin > Integer.parseInt(split[0])) ||
                    (salaryRightMax != null && salaryRightMax < Integer.parseInt(split[1]))) {
                log.info("【月薪不匹配】跳过职位，薪资不匹配, salary: {}, target: {}-{}",
                        salary, salaryLeftMin, salaryRightMax);
                return false;
            }
            if (salary.contains("薪")) {
                // 每年多少薪，比如14薪，如果小于阈值，则跳过
                int salaryCount = Integer.parseInt(salary.split("K")[1]
                        .substring(1).split("薪")[0]);
                if (minSalaryCount != null
                        && salary.split("K").length > 1
                        && salaryCount< minSalaryCount) {
                    log.info("【薪次不匹配】跳过职位，薪资不匹配,{}", salary);
                    return false;
                }
            }
        } catch (Exception e) {
            log.error("过滤薪资异常：{}", e.getMessage(), e);
        }
        return true;
    }
}
