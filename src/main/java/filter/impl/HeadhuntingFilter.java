package filter.impl;

import filter.Filter;
import utils.Job;

public class HeadhuntingFilter implements Filter {


    public static final String HEAD_HUNTING = "猎头";

    @Override
    public boolean doFilter(Job job) {
        if (job.getRecruiter() == null) {
            return true;
        }
        if (job.getRecruiter().contains(HEAD_HUNTING)) {
            return false;
        }
        return true;
    }
}
