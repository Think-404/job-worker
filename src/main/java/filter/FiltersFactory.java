package filter;

import model.FilterContext;

public class FiltersFactory {

    public static Filters getFilters(FilterContext filterContext) {
        return new DefaultFilters(filterContext);
    }
}
