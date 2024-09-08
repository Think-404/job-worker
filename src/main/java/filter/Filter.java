package filter;

import model.FilterContext;
import utils.Job;

public interface Filter {

    default void initialize(FilterContext context) {

    }

    boolean doFilter(Job job);
}
