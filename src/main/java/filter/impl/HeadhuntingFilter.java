package filter.impl;

import cn.hutool.core.collection.CollUtil;
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
        if (CollUtil.isEmpty(job.getTags())) {
            return true;
        }
        return job.getTags().stream().noneMatch(tag -> tag.contains(HEAD_HUNTING));
    }
}
