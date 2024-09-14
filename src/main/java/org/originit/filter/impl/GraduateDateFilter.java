package org.originit.filter.impl;

import lombok.extern.slf4j.Slf4j;
import org.originit.filter.Filter;
import org.originit.model.FilterContext;
import org.originit.utils.Job;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GraduateDateFilter implements Filter {

    @Override
    public boolean doFilter(FilterContext filterContext, Job job) {
        Integer graduateYear = filterContext.getConfiguration().getGraduateYear();
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
