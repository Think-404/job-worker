package org.originit.filter.impl;

import lombok.extern.slf4j.Slf4j;
import org.originit.config.Configuration;
import org.originit.filter.Filter;
import org.originit.model.FilterContext;
import org.originit.utils.Job;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ExperienceFilter implements Filter {

    @Override
    public boolean doFilter(FilterContext filterContext, Job job) {
        Configuration config = filterContext.getConfiguration();
        if (config.getWorkYears() == null) {
            return true;
        }
        Integer workYears = Integer.parseInt(config.getWorkYears());
        String companyTag = job.getCompanyTag();
        if (companyTag == null) {
            return true;
        }
        String s = companyTag;
        if (companyTag.contains("·")) {
            String[] split = companyTag.split("·");
            s = split[0];
        }

        try {
            // 3-5年·本科
            if (s.contains("年")) {
                s = s.split("年")[0];
                if (s.contains("-")) {
                    String[] split1 = s.split("-");
                    if (split1.length == 2) {
                        String min = split1[0].replace("年", "");
                        String max = split1[1].replace("年", "");
                        if (Integer.parseInt(min) <= workYears && workYears <= Integer.parseInt(max)) {
                            log.warn("经验符合要求: {}, {}", companyTag, workYears);
                            return true;
                        } else {
                            log.info("经验不符合要求: {}, {}", companyTag, workYears);
                            return false;
                        }
                    }
                } else {
                    String min = s.replace("年", "");
                    if (Integer.parseInt(min) <= workYears) {
                        return true;
                    } else {
                        log.info("经验不符合要求: {}, {}", companyTag, workYears);
                        return false;
                    }
                }
            } else {
                if (workYears == 0 && s.contains("应届")) {
                    return true;
                }
                if (workYears == -1 && (s.contains("实习") || s.contains("在校"))) {
                    return true;
                }
                log.info("companyTag: {}", companyTag);
            }
        } catch (Exception e) {
            log.error("经验过滤异常: {}, {}", companyTag, e.getMessage(), e);
        }
        log.warn("经验符合要求: {}, {}", companyTag, workYears);
        return true;
    }
}
