package filter.impl;

import filter.Filter;
import lombok.extern.slf4j.Slf4j;
import model.FilterContext;
import utils.Job;

@Slf4j
public class GraduateDateFilter implements Filter {

    private Integer graduateYear = null;

    @Override
    public void initialize(FilterContext context) {
        this.graduateYear = context.getGraduateYear();
    }

    @Override
    public boolean doFilter(Job job) {
        if (graduateYear == null) {
            return true;
        }
        if (job.getGraduateInfo() != null) {
            log.warn("{}: {}", job.getGraduateInfo(), graduateYear);
        }
        if (job.getGraduateYear() == null) {
            return true;
        }
        return job.getGraduateYear().equals(graduateYear);
    }
}
