package filter;

import filter.impl.*;
import lombok.extern.slf4j.Slf4j;
import model.FilterContext;
import utils.Job;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DefaultFilters implements Filters {

    private final List<Filter> filters = new ArrayList<>();

    public DefaultFilters(FilterContext filterContext) {
        filters.add(new GraduateDateFilter());
        filters.add(new HeadhuntingFilter());
        filters.add(new BlackCompanyFilter());
        filters.add(new ExperienceFilter());
        filters.add(new SalaryFilter());
        filters.add(new RecruitTimeFilter());
        filters.add(new AIFilter());
        initialize(filterContext);
    }


    @Override
    public void initialize(FilterContext context) {
        for (Filter filter : filters) {
            filter.initialize(context);
        }
    }

    @Override
    public boolean doFilter(Job job) {
        if (job == null) {
            log.error("Job is null, skip it");
            return false;
        }
        for (Filter filter : filters) {
            try {
                if (!filter.doFilter(job)) {
                    return false;
                }
            } catch (RuntimeException e) {
                log.error("Filter job error: {}", e.getMessage(), e);
                return false;
            }
        }
        return true;
    }
}
