package org.originit.filter;

import org.originit.model.FilterContext;
import org.originit.utils.Job;

public interface Filter {

    boolean doFilter(FilterContext context, Job job);
}
