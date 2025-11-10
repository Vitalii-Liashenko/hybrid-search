package com.liashenko.v.hybrid.search.service;

import com.google.common.base.Stopwatch;
import com.liashenko.v.hybrid.search.config.property.DataLoadingProperties;
import com.liashenko.v.hybrid.search.service.model.Conference;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;


@Slf4j
@AllArgsConstructor
public class CsvDataLoaderService {

    private final DataLoadingProperties properties;
    private final ResourceLoader resourceLoader;
    private final ConferenceCsvParser conferenceCsvParser;
    private final SearchService searchService;

    public void loadToIndex() {
        log.info("Starting data loading from: {}", properties.getDatasetFilePath());

        Resource resource = resourceLoader.getResource(properties.getDatasetFilePath());
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            Stopwatch stopwatch = Stopwatch.createStarted();
            List<Conference> conferences = conferenceCsvParser.parseConferences(reader);
            log.info("Parsed {} conferences from CSV in {}", conferences.size(), stopwatch.stop());

            if (conferences.isEmpty()) {
                throw new IllegalArgumentException("Conferences CSV file is empty or contains no valid data");
            }

            searchService.index(conferences);
        } catch (Exception e) {
            throw new RuntimeException("Error loading Conferences from CSV", e);
        }
    }
}
