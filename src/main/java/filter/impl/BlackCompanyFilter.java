package filter.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import filter.Filter;
import lombok.extern.slf4j.Slf4j;
import model.FilterContext;
import org.json.JSONObject;
import utils.CommonFileUtils;
import utils.Job;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class BlackCompanyFilter implements Filter {

    private final String dataPath = CommonFileUtils.copyClassPathResource("classpath:data.json", "data.json").getAbsolutePath();
    private Set<String> blackCompanies;

    @Override
    public void initialize(FilterContext context) {
        loadData(dataPath);
    }

    private void loadData(String path) {
        try {
            String json = new String(Files.readAllBytes(Paths.get(path)));
            JSONObject jsonObject = new JSONObject(json);
            this.blackCompanies = jsonObject.getJSONArray("blackCompanies").toList().stream().map(Object::toString).collect(Collectors.toSet());
        } catch (IOException e) {
            log.error("读取【{}】数据失败！", path);
        }
        System.out.println(this.blackCompanies);
    }

    @Override
    public boolean doFilter(Job job) {
        if (StrUtil.isBlank(job.getCompanyName())) {
            return true;
        }
        if (CollUtil.isEmpty(blackCompanies)) {
            return true;
        }
        for (String blackCompany : blackCompanies) {
            if (job.getCompanyName().contains(blackCompany)) {
                log.info("【黑名单公司】跳过职位，公司【{}】名包含黑名单公司: {}",
                        job.getCompanyName(), blackCompany);
                return false;
            }
        }
        return true;
    }
}
