package org.originit.filter.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.originit.filter.Filter;
import org.originit.model.FilterContext;
import org.originit.utils.Job;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
public class BlackCompanyFilter implements Filter {

    @Override
    public boolean doFilter(FilterContext context, Job job) {
        Set<String> blackCompanies = context.getConfiguration().getBlackCompanies();
        if (StrUtil.isBlank(job.getCompanyName())) {
            return true;
        }
        if (CollUtil.isEmpty(blackCompanies)) {
            return true;
        }
        for (String blackCompany : blackCompanies) {
            if (job.getCompanyName().toLowerCase().contains(blackCompany.toLowerCase())) {
                log.info("【黑名单公司】跳过职位，公司【{}】名包含黑名单公司: {}",
                        job.getCompanyName(), blackCompany);
                return false;
            }
        }
        return true;
    }
}
