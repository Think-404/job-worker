package org.originit.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.originit.config.AIConfig;
import org.originit.config.Configuration;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilterContext {

    private Configuration configuration;

    private AIConfig aiConfig;

    private Integer salaryLeftMin;

    private Integer salaryRightMax;

    private Integer minSalaryCount;

    private Integer graduateYear;
}
