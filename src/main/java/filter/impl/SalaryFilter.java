package filter.impl;

import filter.Filter;
import lombok.extern.slf4j.Slf4j;
import model.FilterContext;
import utils.Job;

@Slf4j
public class SalaryFilter implements Filter {

    private Integer salaryLeftMin;

    private Integer salaryRightMax;

    private Integer minSalaryCount;
    
    
    @Override
    public void initialize(FilterContext context) {
        this.salaryLeftMin = context.getSalaryLeftMin();
        this.salaryRightMax = context.getSalaryRightMax();
        this.minSalaryCount = context.getMinSalaryCount();
    }

    @Override
    public boolean doFilter(Job job) {
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
