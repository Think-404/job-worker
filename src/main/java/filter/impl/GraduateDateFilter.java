package filter.impl;

import filter.Filter;
import model.FilterContext;
import utils.Job;

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
        if (job.getGraduateYear() == null) {
            return true;
        }
        return job.getGraduateYear().equals(graduateYear);
    }
}
