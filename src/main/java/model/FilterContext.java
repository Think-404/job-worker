package model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilterContext {

    private Integer salaryLeftMin;

    private Integer salaryRightMax;

    private Integer minSalaryCount;

    private Integer graduateYear;
}
