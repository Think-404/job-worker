package org.originit.filter.impl;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;
import org.originit.filter.Filter;
import org.originit.model.FilterContext;
import org.originit.utils.Job;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class HeadhuntingFilter implements Filter {


    public static final String HEAD_HUNTING = "猎头";

    @Override
    public boolean doFilter(FilterContext filterContext, Job job) {
        if (job.getRecruiter() == null) {
            return true;
        }
        if (job.getRecruiter().contains(HEAD_HUNTING)) {
            return false;
        }
        if (CollUtil.isEmpty(job.getTags())) {
            return true;
        }
        boolean isHunting = job.getTags().stream().noneMatch(tag -> tag.contains(HEAD_HUNTING));
        if (isHunting) {
            log.info("【猎头职位】跳过职位，职位【{}】不包含猎头标签", job.getJobName());
        }
        return isHunting;
    }
}
