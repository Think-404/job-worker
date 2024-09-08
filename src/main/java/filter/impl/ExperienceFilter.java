package filter.impl;

import boss.BossConfig;
import filter.Filter;
import lombok.extern.slf4j.Slf4j;
import utils.Job;

@Slf4j
public class ExperienceFilter implements Filter {

    private final BossConfig config = BossConfig.init();

    @Override
    public boolean doFilter(Job job) {
        String workYears = config.getWorkYears();
        if (workYears == null) {
            return true;
        }
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
                        if (Integer.parseInt(min) <= Integer.parseInt(workYears) && Integer.parseInt(workYears) <= Integer.parseInt(max)) {
                            log.warn("经验符合要求: {}, {}", companyTag, workYears);
                            return true;
                        } else {
                            log.info("经验不符合要求: {}, {}", companyTag, workYears);
                            return false;
                        }
                    }
                } else {
                    String min = s.replace("年", "");
                    if (Integer.parseInt(min) <= Integer.parseInt(workYears)) {
                        return true;
                    } else {
                        log.info("经验不符合要求: {}, {}", companyTag, workYears);
                        return false;
                    }
                }
            } else {
                log.info("companyTag: {}", companyTag);
            }
        } catch (Exception e) {
            log.error("经验过滤异常: {}, {}", companyTag, e.getMessage(), e);
        }
        log.warn("经验符合要求: {}, {}", companyTag, workYears);
        return true;
    }
}
