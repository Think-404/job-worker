package org.originit.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.originit.model.FilterContext;
import org.originit.utils.Job;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultFilters implements Filters {

    private final List<Filter> filters;

    @Override
    public boolean doFilter(FilterContext context, Job job) {
        if (job == null) {
            log.error("Job is null, skip it");
            return false;
        }
        if (context == null) {
            log.error("Filter context is null, skip it");
            return false;
        }
        if (context.getConfiguration() == null) {
            log.error("Filter configuration is null, skip it");
            return false;
        }
        for (Filter filter : filters) {
            try {
                if (!filter.doFilter(context, job)) {
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
