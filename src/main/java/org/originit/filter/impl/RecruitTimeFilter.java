package org.originit.filter.impl;

import lombok.extern.slf4j.Slf4j;
import org.originit.filter.Filter;
import org.originit.model.FilterContext;
import org.originit.utils.Job;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class RecruitTimeFilter implements Filter {
    @Override
    public boolean doFilter(FilterContext filterContext, Job job) {
        return recruitTimeValid(job.getRecruitTime());
    }

    public boolean recruitTimeValid(String recruitTime) {
        if (recruitTime == null) {
            return true;
        }
        String[] splitEndTime = recruitTime.split("：");
        if (splitEndTime.length != 2) {
            splitEndTime = recruitTime.split(":");
        }
        if (splitEndTime.length != 2) {
            log.warn("【截止日期格式错误】{}", recruitTime);
            return false;
        } else {
            LocalDateTime endTime = LocalDate.parse(splitEndTime[1], DateTimeFormatter.ofPattern("yyyy.MM.dd")).atStartOfDay();
            if (endTime.isBefore(LocalDateTime.now())) {
                log.warn("【截止日期已过期】{}", recruitTime);
                return false;
            }
        }
        return true;
    }
}
