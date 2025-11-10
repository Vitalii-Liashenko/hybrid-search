package com.liashenko.v.hybrid.search.config.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "data-loading")
@Getter
@Setter
public class DataLoadingProperties {

    private int batchSize;
    private String datasetFilePath;
    private String indexConfigFilePath;
    private ThreadPoolProperties threadPool = new ThreadPoolProperties();

    @Getter
    @Setter
    public static class ThreadPoolProperties {
        private int corePoolSize = 4;
        private int maxPoolSize = 8;
        private int queueCapacity = 100;
        private String threadNamePrefix = "index-";
    }
}
